import random
def onMouseMoved(x, y):
	colInt = random.randrange(1,16777212)
	currentPage.setFillColour("#%08X" % colInt)
	currentPage.hasUpdated()