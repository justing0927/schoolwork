#
# FILE:         $File$
# AUTHOR:       Justin Gonzales, Section #03
#
# DESCRIPTION:
#   This program displays a formatted depiction of the
#   forest fire model grid based on user input
#
# ARGUMENTS:
#   None
#
# INPUT:
#   The size of the grid's edges, the generation, the array
#   representing the current state of the grid
#
# OUTPUT:
#   A visualization of a single generation of the current grid
#
#
 
#-------------------------------
 
#
# Syscall codes
#
  PRINT_STRING = 4
  PRINT_INT = 1
 
#
# DATA AREAS
#
	.data
	.align   0      # string data doesnt need aligning


char_buf:
	.asciiz "0"
	# 30+ char buffer for grid lines

newline2:
	.asciiz "\n"

space2:
	.asciiz " "

pound:
	.asciiz "#"

tree:
	.asciiz "t"
burning:
 	.asciiz "B"
seed:
 	.asciiz "."
grid_corner:
 	.asciiz "+"
grid_side:
 	.asciiz "|"
grid_NS:
	.asciiz "-"
gen_border:
	.asciiz "===="

#-------------------------------

#
# CODE AREA
#
	.text
	.align 2

	.globl	main
	.globl	print_board

#
# Name: print_board
# Description: prints a single generation of a board
# Arguments:	a0:	address of a struct representing the model info
#			- grid size (word)
#			- # of generations (word)
#			- wind direction (asciiz)
#			- addr of an array representing current grid
#		a1:	current generation #
# Returns: None
# Destroys: None
#
print_board:
	addi	$sp, $sp, -16
	sw	$ra, 8($sp)	# store return address
	sw	$s1, 4($sp)
	sw	$s0, 0($sp)	# store s0 (gen num)

	move	$s0, $a0
	move	$s1, $a1	# store a0 and a1 for changes

	jal	print_gen

	lw	$a0, 0($s0)	# move grid size to a0
	jal	print_NS_box

	move	$a0, $s0	# move input struct into a0
	jal	print_grid
	
	lw	$a0, 0($s0)	# move grid size to a0
	jal	print_NS_box

	li	$v0, PRINT_STRING	# print final newline
	la	$a0, newline2
	syscall

print_board_done:
	move	$a0, $s0
	move	$a1, $s1

	lw	$ra, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 16
	jr	$ra

#-------------------------------------

#
# Name: print_gen
# Description: Prints the generation border for current gen
# Arguments: a1 is generation number
# Returns: None
# Destroys: None
#
print_gen:
	# print border
	li	$v0, PRINT_STRING
	la	$a0, gen_border	
	syscall

	# print space
	li      $v0, PRINT_STRING
	la      $a0, space2
	syscall 

	# print pound
	li      $v0, PRINT_STRING
	la      $a0, pound
	syscall 

	# print gen #
	li      $v0, PRINT_INT
	move	$a0, $a1
	syscall 

	# print space
	li      $v0, PRINT_STRING
	la      $a0, space2
	syscall 

	# print border
	li      $v0, PRINT_STRING
	la      $a0, gen_border
	syscall 

	# print newline
	li      $v0, PRINT_STRING
	la      $a0, newline2
	syscall 

	move	$a0, $s0
	move	$a1, $s1

	jr	$ra

#-------------------------------------------------

#
# Name: print_NS_box
# Description: Prints the top and bottom of formatted grid box
# Arguments:	a0 is grid size
# Returns: None
# Destroys: t0, t1
#
print_NS_box:
	addi	$sp, $sp, -8
	sw	$ra, 4($sp)
	sw	$s0, 0($sp)	

	move	$s0, $a0
	# print grid corner
	li      $v0, PRINT_STRING
	la      $a0, grid_corner
	syscall 

	li	$t0, 0
	# for grid size print grid_NS
print_NS_loop:
	slt	$t1, $t0, $s0
	beq	$t1, $zero, NS_loop_done
	
	li      $v0, PRINT_STRING
	la      $a0, grid_NS
	syscall 

	addi	$t0, $t0, 1
	j	print_NS_loop

NS_loop_done:
	# print grid corner
	li      $v0, PRINT_STRING
	la      $a0, grid_corner
	syscall 

	# print newline
	li      $v0, PRINT_STRING
	la      $a0, newline2
	syscall 

	lw	$ra, 4($sp)
	lw	$s0, 0($sp)	# restore s0	

	addi	$sp, $sp, 8
	jr	$ra

#--------------------------------------------------

#
# Name: print_grid
# Description: Loops through array representing the grid, printing
# each of the characters, with '|' character before and after each
# line
# Arguments: a0 as in print_board
# Returns: None
# Destroys: t0, t1, t2, t3, t4, t5
#
print_grid:
	# setup for grid loop
	lw	$t0, 0($a0)	# t0 is grid size
	la	$t4, 12($a0)	# t4 is ptr to array	
	li	$t1, 0		# init loop counter

print_grid_loop:
	# for grid size
	slt	$t2, $t1, $t0
	beq	$t2, $zero, print_grid_loop_done

	# print grid_side
	li      $v0, PRINT_STRING
	la      $a0, grid_side
	syscall 

	# set up for printing line
	li	$t3, 0		# init line loop counter
	la	$a0, char_buf	# set a0 to a char buffer

line_loop:
	# prints each of the characters from the current line
	
	# for grid size
	slt	$t2, $t3, $t0
	beq	$t2, $zero, print_grid_cont

	# get and print current char
	lb	$t5, 0($t4)
	sb	$t5, 0($a0)

	li	$v0, PRINT_STRING
	syscall			# print a0 (current character)

	addi	$t3, $t3, 1	# increment line loop counter
	addi	$t4, $t4, 1	# increment array ptr

	j	line_loop

print_grid_cont:
	# print grid side
	li      $v0, PRINT_STRING
	la      $a0, grid_side
	syscall 

	# print newline
	li      $v0, PRINT_STRING
	la      $a0, newline2
	syscall 
	
	addi	$t1, $t1, 1

	j	print_grid_loop

print_grid_loop_done:
	jr	$ra
