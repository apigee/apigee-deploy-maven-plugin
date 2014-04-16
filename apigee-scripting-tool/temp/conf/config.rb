$xml_files.each { |x| 

  if ( x.file =~ /NullTargetSevice.xml/ ) then
    puts " * * * "
    puts x.file
    puts
    puts x.file
    #x.doc.root.elements.each("//sci:Namespace") { |element| puts element }
  end
}
