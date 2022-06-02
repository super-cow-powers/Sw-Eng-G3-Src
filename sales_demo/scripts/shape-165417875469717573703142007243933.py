def onClick(button, x, y, isDown):
    if isDown:
	screen = doc.getElementByID("text-out").get()
	stack = currentPage.getStateVariable("stack")
	handleAlgebra("**")