def onClick(button, x, y, isDown):
    if isDown:
	screen = doc.getElementByID("text-out").get()
	stack = currentPage.getStateVariable("stack")
	currText = screen.getTextString()
	stack.push(float(currText))
	screen.setText("")
	handleButton("3.14159265")

