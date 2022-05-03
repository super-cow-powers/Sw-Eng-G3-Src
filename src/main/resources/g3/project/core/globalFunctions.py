def onKeyPress(keyName, ctrlDown, altDown, metaDown, keyDown):
    if keyDown == False:
        lowkey = keyName.lower()
        if (lowkey == "left"):
            engine.gotoPrevPage()
        elif (lowkey == "right"):
            engine.gotoNextPage()
        else:
            print("Key " + lowkey + " Pressed. No Action")

def setCursorType(cType):
    engine.setCursorType(cType)
