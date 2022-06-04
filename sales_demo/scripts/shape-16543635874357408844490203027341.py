def onLoad():
	box = doc.getElementByID("scratch-box").get()
	boxB = doc.getElementByID("codecademy-box").get()
	box.setVisibility(False)
	boxB.setVisibility(False)
	box.hasUpdated()
	boxB.hasUpdated()
