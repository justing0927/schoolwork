#
# FILE:         $File$
# AUTHOR:       Justin Gonzales, Section #03
#
# DESCRIPTION:
#   This program take a grid representing the forest fire
#   model and creates a new grid represnting the next generation
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

# NUMERIC CONSTANTS
B_VAL = 66
E_VAL = 46
T_VAL = 116

NTH_VAL = 78
STH_VAL = 83
EST_VAL = 69
WST_VAL = 87

#
# DATA AREA
#
	.data
	.align	2

array_copy:
	.space	930

#
# CODE AREA
#
	.text
	.align	2

	.globl	main
	.globl	update_board

#
# EXECUTION BEGINS HERE
#

#
# Name: update_board
# Description: Manages copying an array representing the board into a new
# array, parsing over the original to update and return the new array
# (the next generation).
# Arguments:	a0:	address of the input struct
#			- a word representing grid size
#			- a word representing # of generations
#			- an asciiz representing wind direction
#			- the address of the array representing the board
# Returns:	v0:	A new array representing the next board generation
# Destroys:
#
update_board:

	addi	$sp, $sp, -32
	sw	$ra, 20($sp)
	sw	$s4, 16($sp)
	sw	$s3, 12($sp)
	sw	$s2, 8($sp)
	sw	$s1, 4($sp)
	sw	$s0, 0($sp)

	move	$s0, $a0
	la	$s1, 12($a0)	# s1 is original array
	lw	$s2, 0($a0)	# s2 is grid size
	lb	$s4, 8($s0)	# s4 is wind direction

	# copy original array to a new array
	move	$a0, $s1
	mul	$a1, $s2, $s2	# a1 is num of elements (grid size^2)
	la	$a2, array_copy	# a2 is new array to copy to
	jal	copy_array
	move	$s3, $a2

	# parse through original for burning trees and update new array
	move	$a0, $s1
	move	$a1, $s3
	move	$a2, $s2
	jal	burn_parse

	move	$a0, $s1
	move	$a1, $s3
	move	$a2, $s2
	move	$a3, $s4
	# parse through original for trees and update new array
	jal	new_tree_parse

	move	$a0, $s3	# copy (from addr of new array)
	mul	$a1, $s2, $s2	# num elements
	move	$a2, $s1	# addr of original array (to copy into)
	# copy new array into original
	jal copy_array
	# original array now updated.

update_board_done:
	move	$a0, $s0	# restore a0

	lw	$ra, 20($sp)
	lw	$s4, 16($sp)
	lw	$s3, 12($sp)
	lw	$s2, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 32
	
	jr	$ra

#--------------------------------------

#
# Name: copy_array
# Description: Copies an array representing the board into a new array
# Argumeints:	a0: address of the array to copy
#		a1: num of elements in array
#		a2: address of array to copy to
#
# Returns:	v0: address of copied array
# Destroys:	t0, t1, t2
#
copy_array:
	li	$t0, 0			# i = 0
	la	$v0, array_copy
	move	$t2, $a2
ca_loop:
	beq	$t0, $a1, ca_done	# done if i == a1
	lb	$t1, 0($a0)		# read from a[i]
	sb	$t1, 0($t2)		# write to v[i]
	
	addi	$a0, $a0, 1		# update src ptr
	addi	$t2, $t2, 1		# update dst ptr
	addi	$t0, $t0, 1		# i++

	j	ca_loop

ca_done:
	jr	$ra

#-------------------------------------

#
# Name: burn_parse
# Description: Parses through original array to update what trees are
# burning in the next generation (the copy array)
# Arguments:	a0: original array addr
#		a1: copied array addr
#		a2: grid size
# Returns: None (Updated array)
# Destroys: t0, t1, t2, t3, t4, t5, t6
#
burn_parse:
	addi	$sp, $sp, -24
	sw	$ra, 20($sp)
	sw	$s4, 16($sp)
	sw	$s3, 12($sp)
	sw	$s2, 8($sp)
	sw	$s1, 4($sp)
	sw	$s0, 0($sp)

	move	$s0, $a0
	move	$s1, $a1
	move	$s2, $a2

	li	$t0, 0			# original array i
	li	$t1, B_VAL		# t1 holds 'B' value
	li	$t4, E_VAL		# t4 holds '.' value
	li	$t5, T_VAL		# t5 holds 't' value
	mul	$t2, $s2, $s2		# t2 is # of grid elements

burn_loop:
	# parse through original array
	beq	$t0, $t2, burn_done
	lb	$t3, 0($s0)		# read from orig[i]
	beq	$t3, $t1, b_tree_found	# found B value

incrm_burn_loop:
	addi	$s0, $s0, 1		# update orig ptr
	addi	$s1, $s1, 1		# update new array ptr
	addi	$t0, $t0, 1		# i++
	j	burn_loop

b_tree_found:
	# set burning trees in original array to empty in old
	sb	$t4, 0($s1)		# update to '.'

	# look at cardinal neighbors of burning trees, if they are in bounds

west_check:	
	# check west neighbor (1 left in array, -1)
	move	$a0, $t0
	li	$a1, -1
	jal	in_bounds
	beq	$v0, $zero, east_check

	# west neighbor in bounds	
	lb	$t6, -1($s0)
	bne	$t5, $t6, east_check
	# west neighbor is 't'
	sb	$t1, -1($s1)		# set west 't' neighbor to 'B'

east_check:
	# check east neighbor
	move	$a0, $t0
	li	$a1, 1
	jal	in_bounds
	beq	$v0, $zero, north_check
	
	# east neighbor in bounds
	lb	$t6, 1($s0)
	bne	$t5, $t6, north_check
	# east neighbor is 't'
	sb	$t1, 1($s1)		# set east 't' neighbor to 'B'

north_check:
	#check north neighbor
	move	$a0, $t0
	mul	$t6, $s2, -1		# mul grid size by -1
	add	$s0, $s0, $t6		# point s0 to -gridsize($s0)
	add	$s1, $s1, $t6		# point s1 to -gridsize($s0)
	move	$a1, $t6		# north is -grid size (-1 row) away
	jal	in_bounds
	beq	$v0, $zero, south_check

	# north neighbor in bounds here, $s0 already in place
	lb	$t6, 0($s0)
	bne	$t5, $t6, south_check
	# north neighbor confirmed to be 't'
	sb	$t1, 0($s1)

south_check:
	add $s0, $s0, $s2		# restore s0 to 0($s0)
	add $s1, $s1, $s2		# restore s1 to 0($s1)

	move	$a0, $t0
	add	$s0, $s0, $s2		# point s0 to gridsize($s0)
	add	$s1, $s1, $s2		# point s1 to gridsize($s1)
	move	$a1, $s2		# south is grid size (1 row) away
	jal	in_bounds
	beq	$v0, $zero, south_check_done

	# south neighbor in bounds
	lb	$t6, 0($s0)
	bne	$t5, $t6, south_check_done
	# south neighbor is 't'
	sb	$t1, 0($s1)

south_check_done:
	mul	$t6, $s2, -1
	add	$s0, $s0, $t6		# restore s0 to 0($s0)
	add	$s1, $s1, $t6		# restore s1 to 0($s1)

	j	incrm_burn_loop

burn_done:
	lw	$ra, 20($sp)
	lw	$s4, 16($sp)
	lw	$s3, 12($sp)
	lw	$s2, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 24
	jr	$ra

#---------------------------------------

#
# Name: new_tree_parse
# Description: Parses through original array to update where new trees
# grow in the the next generation (the copy array).
# Arguments:	a0: original array addr
#		a1: copied array addr
#		a2: grid size
#		a3: wind direction	
# Returns: None (Updates array)
# Destroys: t0, t1, t2, t3, t4, t5, t6
#
new_tree_parse:
	addi	$sp, $sp, -24
	sw	$ra, 20($sp)
	sw	$s4, 16($sp)
	sw	$s3, 12($sp)
	sw	$s2, 8($sp)
	sw	$s1, 4($sp)
	sw	$s0, 0($sp)

	move	$s0, $a0
	move	$s1, $a1
	move	$s2, $a2

	li	$t0, 0		# original array i
	li	$t4, E_VAL	# t4 holds '.' value
	li	$t5, T_VAL	# t5 holds 't' value
	mul	$t2, $s2, $s2	# t2 is # of grid elements

tree_loop:
	# parse through original array
	beq	$t0, $t2, tree_done
	lb	$t3, 0($s0)		# read from orig[i]
	beq	$t3, $t5, tree_found	# found t value

incrm_tree_loop:
	addi    $s0, $s0, 1	# update orig ptr
	addi	$s1, $s1, 1	# update new array ptr
	addi	$t0, $t0, 1	# i++

	j	tree_loop

tree_found:
	# look in wind direction of tree
	move	$s3, $s0
	move	$s4, $s1	# save s0 and 1 if modified by north or south	

	li	$t1, NTH_VAL
	beq	$t1, $a3, t_north_check

	li	$t1, STH_VAL
	beq	$t1, $a3, t_south_check

	li	$t1, WST_VAL
	beq	$t1, $a3, t_west_check

	li	$t1, EST_VAL
	beq	$t1, $a3, t_east_check

t_west_check:
        # check west neighbor (1 left in array, -1)
        move    $a0, $t0
        li      $a1, -1
        jal     in_bounds
        beq     $v0, $zero, t_check_done

        # west neighbor in bounds
        lb      $t6, -1($s0)
        bne     $t4, $t6, t_check_done
        # west neighbor is '.'
        sb      $t5, -1($s1)            # set west '.' neighbor to 't'
	j	t_check_done

t_east_check:
        # check east neighbor
        move    $a0, $t0
        li      $a1, 1
        jal     in_bounds
        beq     $v0, $zero, t_check_done

        # east neighbor in bounds
        lb      $t6, 1($s0)
        bne     $t4, $t6, t_check_done
        # east neighbor is '.'
        sb      $t5, 1($s1)             # set east '.' neighbor to 't'
	j	t_check_done

t_north_check:
        #check north neighbor
        move    $a0, $t0
        mul     $t6, $s2, -1            # mul grid size by -1
        add     $s0, $s0, $t6           # point s0 to -gridsize($s0)
        add     $s1, $s1, $t6           # point s1 to -gridsize($s0)
        move    $a1, $t6                # north is -grid size (-1 row) away
        jal     in_bounds
        beq     $v0, $zero, t_check_done

        # north neighbor in bounds here, $s0 already in place
        lb      $t6, 0($s0)
        bne     $t4, $t6, t_check_done
        # north neighbor confirmed to be '.'
        sb      $t5, 0($s1)
	j	t_check_done

t_south_check:
        move    $a0, $t0
        add     $s0, $s0, $s2           # point s0 to gridsize($s0)
        add     $s1, $s1, $s2           # point s1 to gridsize($s1)
        move    $a1, $s2                # south is grid size (1 row) away
        jal     in_bounds
        beq     $v0, $zero, t_check_done

        # south neighbor in bounds
        lb      $t6, 0($s0)
        bne     $t4, $t6, t_check_done
        # south neighbor is '.'
        sb      $t5, 0($s1)

t_check_done:
	move	$s0, $s3
	move	$s1, $s4
	j	incrm_tree_loop	

tree_done:
	lw	$ra, 20($sp)
	lw	$s4, 16($sp)
	lw	$s3, 12($sp)
	lw	$s2, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 24
	jr	$ra

#---------------------------------------

#
# Name: in_bounds
# Description: Determines if, given the location in a 900 byte array
# and the location to check, the destination is in bounds of that array
# Arguments:	a0: Destination (From 0 to (grid size^2 - 1))
#		a1: Relative neighbor location (negative or positive int)
#		a2: grid size
# Returns:	v0: 1 if valid, 0 if not.
# Destroys: 	t9
#
in_bounds:
	addi	$sp, $sp, -16
	sw	$ra, 8($sp)
	sw	$s1, 4($sp)
	sw	$s0, 0($sp)

	move	$s1, $a2
	mul	$s1, $s1, $s1		
	addi	$s1, $s1, -1		# s1 holds upper bound
	add	$v0, $a0, $a1		# v0 hold destination

	slt	$s0, $v0, $zero		# check if less than 0
	bne	$s0, $zero, out_of_bounds

	slt	$s0, $s1, $v0		# check if greater than max size
	bne	$s0, $zero, out_of_bounds
	
	# check if dest % grid size = grid size - 1.
	rem	$v0, $a0, $a2
	addi	$t9, $a2, -1
	beq	$v0, $t9, check_right_bound

cont_check:
	# check if dest % grid size = 0. 
	rem	$v0, $a0, $a2
	beq	$v0, $zero, check_left_bound 

check_all_done:
	# in bounds
	li	$v0, 1
	lw	$ra, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 16
	# restore saved regs and stack ptr, set v0
	jr	$ra

check_right_bound:
	# if neighbor loc is 1, invalid
	li	$t9, 1
	beq	$a1, $t9, out_of_bounds
	j	cont_check

check_left_bound:
	# If neighbor loc is -1, out of bounds
	li	$t9, -1
	beq	$a1, $t9, out_of_bounds
	j check_all_done

out_of_bounds:
	li	$v0, 0
	lw	$ra, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 16
	# restore saved regs, stack ptr, set v0
	jr	$ra
