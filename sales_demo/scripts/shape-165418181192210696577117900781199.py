def onClick(button, x, y, isDown):
	import math
    	if isDown:
		helpBox = findElement("calchelpbox")
		vis = not helpBox.getVisibility()
		helpBox.setVisibility	(vis)
		helpBox.hasUpdated()

