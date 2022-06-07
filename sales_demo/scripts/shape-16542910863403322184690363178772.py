import random
def onMouseMoved(x, y):
	colInt = random.randrange(1,16777212)
	this.setFillColour("#%06X" % colInt)
	this.setTextColour("#%06X" % (16777212 - colInt))
	this.hasUpdated()