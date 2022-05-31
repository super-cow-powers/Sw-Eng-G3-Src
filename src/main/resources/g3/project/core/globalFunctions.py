def gotoNextPage():
    engine.gotoNextPage()

def gotoPrevPage():
    engine.gotoPrevPage()

def onKeyPress(keyName, ctrlDown, altDown, metaDown, keyDown):
    if keyDown == False:
        lowkey = keyName.lower()
        if (lowkey == "left"):
            engine.gotoPrevPage()
        elif (lowkey == "right"):
            engine.gotoNextPage()
        else:
            print("Key " + lowkey + " Pressed.")

def setCursorType(cType):
    engine.setCursorType(cType)    
    
def quit():
    engine.exit()

