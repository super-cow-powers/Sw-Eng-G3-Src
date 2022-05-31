import g3.project.elements.ShapeElement as ShapeElement

BASE_URI = "http://PWS_Base"
EXT_URI = "http://PWS_Exts"
defsz = 100
el = "movingrect"

def onLoad():
    engine.putShape(el, "rectangle")
    engine.setShapeStroke(el, "black", "plain", 5)
    engine.setShapeColour(el, "transparent")
    engine.resizeElement(el, defsz, defsz, 0)
    engine.setElementVisibility(el, False)

def onMouseMoved(x, y):
    engine.setElementVisibility(el, True)
    engine.moveElement(el, x, y, -100)
    global xCurrent
    xCurrent = x
    global yCurrent
    yCurrent = y

def onClick(button, x, y, isDown):
    global xCurrent
    global yCurrent
    if isDown:
        page = doc.getCurrentPage().get()
        shId = doc.getNewUniqueID("shape")
        shEl = ShapeElement("base:shape", BASE_URI)
        page.appendChild(shEl)
        shEl.setType("rectangle")
        shEl.setOriginXY(xCurrent, yCurrent)
        shEl.setID(shId)
        shEl.setSize(defsz, defsz, 0)
        shEl.setFillColour("transparent")
        shEl.setStroke("black", "plain", 5)
        shEl.hasUpdated()
        print(str(xCurrent) + "," + str(yCurrent))

def onClose():
    engine.removeElementFromScreen(el)
