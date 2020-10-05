#
# Makefile for CCS Project - forest fire
#
#
# Location of the processing programs
#
RASM  = /home/fac/wrc/bin/rasm
RLINK = /home/fac/wrc/bin/rlink
RSIM = /home/fac/wrc/bin/rsim

#
# Suffixes to be used or created
#
.SUFFIXES:  .asm .obj .lst .out

#
# Transformation rule: .asm into .obj
#
.asm.obj:
	$(RASM) -l $*.asm > $*.lst

#
# Transformation rule: .obj into .out
#
.obj.out:
	$(RLINK) -m -o $*.out $*.obj > $*.map

#
# Object Files
#
OBJECTS = forestfire.obj print_board.obj update_board.obj

#
# Main Target
#
forest_fire.out: $(OBJECTS)
	$(RLINK) -m -o forestfire.out $(OBJECTS) > forestfire.map

