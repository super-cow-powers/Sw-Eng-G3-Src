import random
def rotBox():
	from time import sleep
	aboutBox = findElement("shape-1654185021998")
	vis = aboutBox.getVisibility()
	dir = random.choice([-1,1])
	while vis and (currentPage.getID() == "page-1654184621878"):
		sleep(0.05)
		vis = aboutBox.getVisibility()
		sz = aboutBox.getSize().get()
		rot = (sz.getRot() + (.5*dir)) % 360
		aboutBox.setSize(sz.getX(), sz.getY(), rot)
		aboutBox.hasUpdated()
	aboutBox.setVisibility(False)
	sz = aboutBox.getSize().get()
	aboutBox.setSize(sz.getX(), sz.getY(), 0)


