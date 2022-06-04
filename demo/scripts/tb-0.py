def onClick(button, x, y, mouseDown):
    print("Hello from " + this.getID())
    if mouseDown == True:
        this.setFillColour("#123456ff")
        print("Mouse is down")
    else:
        this.setFillColour("#A2A2A2FF")
        print("Mouse is up")
        gotoNextPage()

