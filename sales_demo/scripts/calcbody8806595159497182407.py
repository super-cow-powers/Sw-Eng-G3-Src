def onLoad():
	ret = findElement("cbret")
	screen = doc.getElementByID("text-out").get();
	screen.setText("0")
	ret.putStateVariable("pressed", True);
	screen.hasUpdated()