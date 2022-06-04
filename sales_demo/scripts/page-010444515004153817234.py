def onLoad():
	from java.util import ArrayDeque
	if this.getStateVariable("stack") is None:
		this.putStateVariable("stack", ArrayDeque())

def handleButton(buttonStr):
	enter = doc.getElementByID("cbret").get()
	enterPressed = enter.getStateVariable("pressed")
	screen = doc.getElementByID("text-out").get()
	if enterPressed:
		enter.putStateVariable("pressed", False)
		screen.setText(buttonStr)
	else:
		screen.setText(screen.getTextString() + buttonStr)
	screen.hasUpdated()

def handleAlgebra(signStr):
	screen = doc.getElementByID("text-out").get()
	x = screen.getTextString()
	stack = currentPage.getStateVariable("stack")
	if stack.size() >= 1:
		res = eval(str(stack.pop()) + signStr + x)
		stack.push(float(res))
		screen.setText(str(res))
		enter = doc.getElementByID("cbret").get()
		enter.putStateVariable("pressed", True)
		screen.hasUpdated()


