import g3.project.elements.ShapeElement as ShapeElement

BASE_URI = "http://PWS_Base"
EXT_URI = "http://PWS_Exts"
defsz = 100
el = "movingshape"
shtype = ""

def onLoad():
    print("Select Shape. Available are:\nRectangle\nEllipse")
    global shtype
    shtype = engine.readConsoleLine().strip().lower()
    while (shtype != "rectangle") and (shtype != "ellipse"):
        print("I don't know " + shtype + "\n Select Shape. Available are:\nRectangle\nEllipse")
        shtype = engine.readConsoleLine().strip().lower()
    engine.putShape(el, shtype)
    engine.drawShapeStroke(el, "black", "plain", 5)
    engine.setShapeColour(el, "transparent")
    engine.resizeElement(el, defsz, defsz, 0)
    engine.setElementVisibility(el, False)

def onMouseMoved(x, y):
    engine.setElementVisibility(el, True)
    engine.moveElement(el, x, y, -100)

def onClick(button, x, y, isDown):
    global shtype
    if isDown:
        page = doc.getCurrentPage().get()
        shId = doc.getNewUniqueID("shape-")
        shEl = ShapeElement("base:shape", BASE_URI)
        page.appendChild(shEl)
        shEl.setType(shtype)
        shEl.setOriginXY(x, y)
        shEl.setID(shId)
        shEl.setSize(defsz, defsz, 0)
        shEl.setFillColour("transparent")
        shEl.setStroke("black", "plain", 5)
        shEl.hasUpdated()

def onClose():
    engine.removeElementFromScreen(el)
