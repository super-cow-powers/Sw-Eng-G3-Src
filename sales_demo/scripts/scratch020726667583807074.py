def onClick(button, x, y, isDown):
	box = doc.getElementByID("scratch-box").get()
	boxB = doc.getElementByID("codecademy-box").get()
	vis = box.getVisibility()
	if not vis:
		box.setVisibility(True)
		box.setOriginXY(160, 200)
		boxB.setVisibility(False)
	else:
		box.setVisibility(False)
	box.hasUpdated()
	boxB.hasUpdated()

