def onClick(button, x, y, isDown):
    if isDown:
	screen = doc.getElementByID("text-out").get()
	currentText = screen.getTextString()
	stack = currentPage.getStateVariable("stack")
	textBits = currentText.split("E")
	setText = ""
	if len(textBits) == 2:
		#Has an exponent
		exponent = textBits[1]
		if "-" in exponent:
			exponent = exponent.replace('-', '')
		else:
			exponent = "-" + exponent
		setText = textBits[0] + "E" + exponent
	else:
		if "-" in textBits[0]:
			setText = textBits[0].replace('-','')
		else:
			setText = "-"+textBits[0]
	screen.setText(setText)
	screen.hasUpdated()
	
			
		


