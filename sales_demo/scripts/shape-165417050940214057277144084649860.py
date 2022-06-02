
def onClick(button, x, y, isDown):
    if isDown:
	screen = doc.getElementByID("text-out").get()
	stack = currentPage.getStateVariable("stack")
	currText = screen.getTextString()
	if stack.size() >= 2:
		#Roll-down Y->X, X->End
		y = stack.pop()
		stack.add(float(currText))
		screen.setText(str(y))
		screen.hasUpdated()
