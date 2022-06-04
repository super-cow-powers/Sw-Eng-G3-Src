
def rotBox():
	from time import sleep
	aboutBox = findElement("shape-1654185021998")
	vis = aboutBox.getVisibility()
	while vis:
		sleep(0.05)
		s = currentPage.getStateVariable("s")
		vis = aboutBox.getVisibility()
		sz = aboutBox.getSize().get()
		rot = (sz.getRot() + 1) % 360
		aboutBox.setSize(sz.getX(), sz.getY(), rot)
		aboutBox.hasUpdated()

