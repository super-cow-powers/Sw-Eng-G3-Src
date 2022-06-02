buttonText = "."
def onClick(button, x, y, isDown):
    if isDown:
	enter = doc.getElementByID("cbret").get()
	enterPressed = enter.getStateVariable("pressed")
	screen = doc.getElementByID("text-out").get()
	if not enterPressed:
		currText = screen.getTextString()
		if not "." in currText:
			screen.setText(currText + buttonText)
			screen.hasUpdated()

