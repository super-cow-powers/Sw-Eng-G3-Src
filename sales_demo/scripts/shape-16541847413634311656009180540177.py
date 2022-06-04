def pr(s):
	print(s)

def onClick(button, x, y, isDown):
	from threading import Thread
	import math
    	if isDown:
		import sched, time
		s = currentPage.getStateVariable("s")
		if s is None:
			s = sched.scheduler(time.time, time.sleep)
			currentPage.putStateVariable("s", s)
		aboutBox = findElement("shape-1654185021998")
		vis = not aboutBox.getVisibility()
		aboutBox.setVisibility(vis)
		aboutBox.hasUpdated()
		#rotBox()
		if vis :
			Thread(target=lambda: rotBox()).start()


	