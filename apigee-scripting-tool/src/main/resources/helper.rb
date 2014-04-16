class AstXmlHolder
    attr_accessor :doc,:file,:log,:profile,:yaml,:bundle_configurer
    
    def check_for_tabs(file)
        line_num = 1
        f = File.new(file, "r")
        while (line = f.gets)
            if line.include? "\t"
                raise "#{file}:#{line_num} - yml files cannot contain tabs"
            end
            line_num = line_num+1
        end
        f.close

    end

    def parse_yaml(file)
        check_for_tabs(file)
        @yaml = YAML::load(File.open(file))
        if @yaml == nil || @yaml == false
            raise "Could not parse config.yml: #{file}"
        end
    end

    def parse_xml(xml_file)
        @file = xml_file
        @doc = Document.new IO.read(@file)
        @doc.context[:attribute_quote] = :quote
        @ns = { 'soap_http' => 'http://www.sonoasystems.com/schemas/2007/8/3/soap/http/',
                'http_1' => 'http://www.sonoasystems.com/schemas/2007/8/3/http/',
                'http' => 'http://www.sonoasystems.com/schemas/2007/8/3/soap/http/',
                'sci'=>'http://www.sonoasystems.com/schemas/2007/8/3/sci/',
                'wsdl'=>'http://schemas.xmlsoap.org/wsdl/',
                'soap'=>'http://schemas.xmlsoap.org/wsdl/soap/',
                'shttp'=> 'http://www.sonoasystems.com/schemas/2007/8/3/http/' }
    end

    def each(xpath)
        XPath.each(@doc,xpath,@ns) { |x|
            yield x
        }
    end

    def delete_all( xpath)

        XPath.each(@doc.root, xpath, @ns) { |it| it.parent.delete it }
        # @doc.root.elements.delete_all xpath, @ns

    end

    def set_text(xpath, value)

        XPath.each(@doc,xpath,@ns) { |element|
            element.each { |x| x.parent.delete x }
            element.add_text expr(value)
        }

    end

    def set_attribute(xpath, name, value)

        XPath.each(@doc,xpath,@ns) { |element|
            element.attributes[name] = expr(value)
        }

    end

    def configure_virtual_hosts(endpoint_name, vhosts)
	    $ASTLOG.debug "Setting virtual hosts:  #{vhosts}"
        # Delete all the existing VirtualHost entries
        	#delete_all "//http_1:VirtualHost"
		delete_all "//sci:ClientEndpoint[@name='#{endpoint_name}']/http:ClientAddress/http:VirtualHost"
		#delete_all "//sci:ClientEndpoint[@name='#{endpoint_name}']/sci:Metadata"
		delete_all "//sci:ClientEndpoint[@name='#{endpoint_name}']/http_1:ClientAddress/http_1:VirtualHost"

        expr(vhosts).each { |vhost|
            XPath.each(@doc,"//sci:ClientEndpoint[@name='#{endpoint_name}']/http:ClientAddress",@ns) { |addr|
                vh = Element.new "http:VirtualHost"
                addr.insert_before(addr.elements[1],vh);
                #vh.add_namespace("http://www.sonoasystems.com/schemas/2007/8/3/http/")
                vh.add_attribute('ref',vhost)
            }
        }
        expr(vhosts).each { |vhost|
            XPath.each(@doc,"//sci:ClientEndpoint[@name='#{endpoint_name}']/http_1:ClientAddress",@ns) { |addr|
                vh = Element.new "http_1:VirtualHost"
                addr.insert_before(addr.elements[1],vh);
                #vh.add_namespace("http://www.sonoasystems.com/schemas/2007/8/3/http/")
                vh.add_attribute('ref',vhost)
            }
        }

    end

    def expr(key)
        if key.class.to_s != 'String'
            return key
        end

        r = /\$\{(.*)\}/
        m = r.match key
        if m == nil
            return key
        else
            expanded_val = bundle_configurer.eval_expression(get_cfg(m[1]))
            return expanded_val
     
        end
    end

    def set_target_endpoint_uri(endpoint_name, target_uri)

        $ASTLOG.debug "Replacing target endpoint URI for '#{endpoint_name}' with '#{target_uri}'"

        each("//sci:TargetEndpoint[@name='#{endpoint_name}']/http_1:TargetAddress/http_1:URI") {|te|
            begin
                te.text=expr(target_uri)
            rescue Exception => e
                $ASTLOG.error e
            end
        }
        each("//sci:TargetEndpoint[@name='#{endpoint_name}']/soap_http:TargetAddress/soap_http:URI") {|te|
            begin
                te.text=expr(target_uri)
            rescue Exception => e
                $ASTLOG.error e
            end
        }
    end
    
    def set_target_address(endpoint_name, target_address_hash)
       
        if target_address_hash.has_key?("port")
            xpath =  "//sci:TargetEndpoint[@name='#{endpoint_name}']/shttp:TargetAddress/shttp:Port"
            each(xpath) {|te|
            begin
                te.text=expr(target_address_hash['port'])
           rescue Exception => e
                $ASTLOG.error e
           end
            }
        end
        
        if target_address_hash.has_key?("path")
            xpath =  "//sci:TargetEndpoint[@name='#{endpoint_name}']/shttp:TargetAddress/shttp:Path"
            
            each(xpath) {|te|
                begin
                    te.text=expr(target_address_hash['path'])
                rescue Exception => e
                    $ASTLOG.error e
                end
            }
        end
        
    end

    def configure_policy(policy_name, policy_hash)
        $ASTLOG.debug "Configuring policy '#{policy_name}'"
        # If "enabled" is specified, go ahead and override
        if policy_hash.has_key?('enabled')
            $ASTLOG.debug "Setting #{policy_name} enabled=#{policy_hash['enabled']}"
            XPath.each(@doc,"//sci:Policy[@name='#{policy_name}']",@ns) { |el|
                el.attributes['enabled'] = policy_hash['enabled']
            }
        end

        # If there is a section named 'variables', loop through and set them on a per-policy basis
        if policy_hash.has_key?('variables')
            # OK, we have variables to set
            $ASTLOG.debug "Setting variables for policy: #{policy_name}"
            policy_hash['variables'].each  { |k,v|
                configure_policy_variable k,v,policy_name
            }
        end

    end

    def configure_policy_variable (key, val, policy_name=nil)

        if val != nil
			expanded_val = bundle_configurer.eval_expression(val.to_s)
            if policy_name
                xpath = "//sci:Policy[@name='#{policy_name}']/sci:Variables/sci:Variable[@name='#{key}']"
            else
                xpath = "//sci:Variables/sci:Variable[@name='#{key}']"
            end

            $ASTLOG.debug "xpath: #{xpath}"
            XPath.each(@doc,xpath,@ns) { |var|

                var.attributes['value']=expanded_val
                $ASTLOG.debug "Override value='#{val.to_s}': #{expanded_val}"
            }
        end
    end

    def dump_policy_variables
        tmp = {}
        log.debug "# Dumping all variables from: #{file}"
        XPath.each(@doc,'//sci:Variable',@ns) { |var|
            log.debug "#{var.attributes['name']}: #{var.attributes['value']}"
        }
    end

    def configure_target_endpoint(epn,te)
        $ASTLOG.debug "configuring target endpoint: #{epn}"
        if te.class != Hash
            raise "target endpoint is misconfigured in config.yml"
        end
        if te.has_key?('uri')
            set_target_endpoint_uri epn , te['uri']
        end
        if te.has_key?("targetaddress") 
            set_target_address epn, te['targetaddress']
        end
    end

    def configure_client_endpoint(epn,te)
        $ASTLOG.debug "configuring client endpoint: #{epn}"
        if te == nil
            return
        end
        if te.has_key?('vhost')
            configure_virtual_hosts epn, te['vhost']
        end
    end

    def match_file(regex)
        val = @file =~ regex
        if val == nil
            #debug "No match on #{@file}"
        else
            $ASTLOG.debug "Match: #{@file}"
        end
        return val
    end

    def execute
        $ASTLOG.debug "Processing with profile '#{@profile}'"
        profile = @yaml[@profile]
        if profile == nil
            raise "Selected profile '#{@profile}' not found in config.yml"
        end
        if profile['target_endpoints']
            profile['target_endpoints'].each { |epn, vals|
                configure_target_endpoint(epn,vals)
            }
        end

        if profile['client_endpoints']
            profile['client_endpoints'].each { |epn, vals|
                configure_client_endpoint(epn,vals)
            }
        end

        if profile['variables']
            profile['variables'].each { |k,v|
                configure_policy_variable k,v
            }

        end
        if profile['policies']
            profile['policies'].each { |k,v|
                configure_policy k, v
            }
        end
        write_file
    end

    def write_file
        File.open(@file,'w') {|file| file.write(@doc)}
    end
end
