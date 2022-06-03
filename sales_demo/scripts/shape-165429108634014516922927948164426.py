import random
def onMouseMoved(x, y):
	colInt = random.randrange(1,16777212)
	this.setFillColour("#%08X" % colInt)
	this.hasUpdated()