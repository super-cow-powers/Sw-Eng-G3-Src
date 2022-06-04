def onClick(button, x, y, isDown):
	import math
    	if isDown:
		stack = currentPage.getStateVariable("stack")
		screen = doc.getElementByID("text-out").get()
		currText = screen.getTextString()
		rt = math.sqrt(float(currText))
		stack.push(float(rt))
		screen.setText(str(rt))
		screen.hasUpdated()

