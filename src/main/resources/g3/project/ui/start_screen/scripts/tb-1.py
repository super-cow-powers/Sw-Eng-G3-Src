def onClick(button, x, y, mouseDown):
    if mouseDown == True:
        engine.loadEmptyDoc()

    
def onMouseEnter(x,y):
    #print("Mouse Entered " + this.toString() + " at: (" + str(x) + "," + str(y) + ")")
    this.setFillColour("#AAAAAAFF")
    this.hasUpdated()
    setCursorType("HAND")

def onMouseExit(x,y):
    #print("Mouse Left " + this.toString() + " at: (" + str(x) + "," + str(y) + ")")
    this.setFillColour("#EEEEEEFF")
    this.hasUpdated()
    setCursorType("DEFAULT")
