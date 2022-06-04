def onClick(button, x, y, isDown):
	import math
    	if isDown:
		stack = currentPage.getStateVariable("stack")
		screen = doc.getElementByID("text-out").get()
		currText = screen.getTextString()
		sine = math.sin(float(currText))
		stack.push(float(sine))
		screen.setText(str(sine))
		screen.hasUpdated()
