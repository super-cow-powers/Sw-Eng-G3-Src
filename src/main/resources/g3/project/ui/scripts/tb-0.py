def onClick(button, x, y, mouseDown):
    if mouseDown == True:
        #I can't make this button do anything fancy, as the picker blocks the mouse release event
        #Show only on mouse down
        engine.showDocChooser()

    
def onMouseEnter(x,y):
    print("Mouse Entered " + this.toString() + " at: (" + str(x) + "," + str(y) + ")")
    this.setFillColour("#AAAAAAFF")
    setCursorType("HAND")

def onMouseExit(x,y):
    print("Mouse Left " + this.toString() + " at: (" + str(x) + "," + str(y) + ")")
    this.setFillColour("#EEEEEEFF")
    setCursorType("DEFAULT")
