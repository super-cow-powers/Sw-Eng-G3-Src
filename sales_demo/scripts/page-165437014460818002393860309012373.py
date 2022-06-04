def fadeIn():
	from time import sleep
	clickBox = findElement("shape-1654370236419")
	strat = [findElement("strat0"), findElement("strat1"), findElement("strat2"), findElement("strat3")]
	clickBox.putStateVariable("fading", True)
	stop = False
	i = 0
	opacity = 0.0001
	while (currentPage.getID() == "page-1654370144608") and (stop == False):
		strat[i].setAlpha(opacity)
		strat[i].setTextAlpha(opacity)
		strat[i].hasUpdated()
		opacity = opacity + 0.01
		sleep(0.05)
		if opacity >= 1:
			opacity = 0.001
			i = i+1
			if i > len(strat)-1:
				stop = True
	clickBox.putStateVariable("fading", False)
