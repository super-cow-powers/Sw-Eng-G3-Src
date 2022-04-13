def onClick(button, x, y, mouseDown):
    if mouseDown == True:
        #I can't make this button do anything fancy, as the picker blocks the mouse release event
        #Show only on mouse down
        engine.showDocChooser()

    
