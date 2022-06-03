def onClick(button, x, y, isDown):
    if isDown:
	screen = doc.getElementByID("text-out").get()
	stack = currentPage.getStateVariable("stack")
	currText = screen.getTextString()
	if stack.size() >= 1:
		#X<->Y swap
		y = stack.pop()
		stack.push(float(currText))
		screen.setText(str(y))
		screen.hasUpdated()
