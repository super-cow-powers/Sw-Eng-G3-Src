from threading import Thread
def onClick(button, x, y, isDown):
	fading = this.getStateVariable("fading")
	if fading is None:
		this.putStateVariable("fading", False)
		fading = False
	if isDown:
		this.setFillColour("#b3b3b3")
		if not fading:
			Thread(target=lambda: fadeIn()).start()
	else:
		this.setFillColour("#f2f2f2")
	this.hasUpdated()

def onLoad():
	strat = [findElement("strat0"), findElement("strat1"), findElement("strat2"), findElement("strat3")]
	for s in strat:
		s.setAlpha(0)
		s.setTextAlpha(0)
		s.hasUpdated()
