xml_files.each { |x| 
	return
	
	x.configure_policy_variables
	if x.match_file /NullTargetService.xml/
		
		x.delete_all "//HttpMethod[text()='TRACE']"
		
		vh = XPath.first(x.doc,"//http_1:ClientAddress").add_element "VirtualHost"
	#	x.each("//sci:Operation")  { |el| puts el }
		x.set_attribute  "//sci:Operation","name", x.expr("${test}")
		puts x.doc
		#XPath.each(x.doc,"//HttpMethod[text() = 'TRACE']") { |it| puts it }
	end


}
 