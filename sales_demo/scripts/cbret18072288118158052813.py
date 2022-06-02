def onClick(button, x, y, isDown):
    if isDown:
	screen = doc.getElementByID("text-out").get()
	stack = currentPage.getStateVariable("stack")
	this.putStateVariable("pressed", True)
	currentPage.putStateVariable("expMode", False)
	stack.push(float(screen.getTextString()))

