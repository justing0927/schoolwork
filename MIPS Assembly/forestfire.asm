#
# FILE:         $File$
# AUTHOR:       Justin Gonzales, Section #03
#
# DESCRIPTION:
#   This program is an implementation of the forest fire model
#   cellular automata in MIPS assembly
#   
# ARGUMENTS:
#   None
#
# INPUT:
#   The size of the grid's edges, the # of generations to run,
#   the wind direction, and the 0th generation grid from STDIN
#
# OUTPUT:
#   A visualization of the following generations of the inputted
#   grid - complete with formatted banner and fluff.
#
#

#-------------------------------
 
#
# Numeric Constants and syscall codes
#
GRID_MIN = 4
GRID_MAX = 30
GEN_MIN = 0
GEN_MAX = 20
PRINT_STRING = 4
READ_INT = 5
PRINT_INT = 1
READ_STRING = 8
#READ_CHAR = 12

#
# DATA AREAS
#
	.data
	.align  2       # word data must be on word boundaries

input_block:
	.word   -1		# the grid size
	.word   -1	     	# the num of generations
 	.asciiz "Q"     	# the wind direction
	.word	array		# set of strings

# need either to put together 30 30-byte arrays, or 1 900 byte array
# null terms at end of lines for string printing?
# index by grid size
array:
	.space 930


	.align	0	# string data doesn't have to be aligned

fig_char:
	.asciiz "01234567898765432123456789876543210"
	# not a char but similar to what was provided in lab4
	# buffer for reading in string

space:
	.asciiz " "

newline:
	.asciiz "\n"

grid_error_str:
	.asciiz "ERROR: invalid grid size\n"

gen_error_str:
	.asciiz "ERROR: invalid number of generations\n"

wind_error_str:
	.asciiz "ERROR: invalid wind direction\n"

bad_char_error_str:
	.asciiz	"ERROR: invalid character in grid\n"

banner_outline:	# remember newline after second one
	.asciiz "+-------------+"

banner_middle:
	.asciiz	"| FOREST FIRE |

#-------------------------------

#
# CODE AREA
#
	.text
	.align	2

	.globl	main
	.globl	update_board
	.globl	print_board

#
# EXECUTION BEGINS HERE
#
main:
	addi	$sp, $sp, -4	# allocate space for return addr
	sw	$ra, 0($sp)

	jal	output_banner	# output starting banner and \n

	la	$a0, input_block
	la	$t0, array
	sw	$t0, 12($a0)	# set up parameter struct

	la	$a1, GRID_MAX	# max num of lines possible
	jal	read_input
	
	# a0 now contains all supplied input

	jal	error_check

	bne	$v0, $zero, main_done	# exit if errors found
	
	# input confirmed to be valid

	li	$s0, 0		# s0 represents current generation #
	move	$a1, $s0	# move s0 into a1 for initial printing

	jal	print_board	# print initial board

	lw	$s1, 4($a0)	# s1 holds num of generations
	addi	$s1, $s1, 1	# add 1 to account for less than

main_gen_loop:
	# loop for each generation
	addi	$s0, $s0, 1	# increment which generation
	slt	$t1, $s0, $s1
	beq	$t1, $zero, main_done

	# update the board
	jal update_board

	# original array should now be updated to be new array

	# print the updated board
	move	$a1, $s0
	jal	print_board

	j	main_gen_loop

main_done:
	lw	$ra, 0($sp)
	addi	$sp, $sp, 4
	jr	$ra
#-------------------------------

#
# Name:     output_banner
# Description:  prints the starting banner to stdout
# Arguments:    none
# Returns:      none
# Destroys:     none
#
output_banner:
	addi	$sp, $sp, -4
	sw	$ra, 0($sp)

	li	$v0, PRINT_STRING
	la	$a0, banner_outline
	syscall

	li	$v0, PRINT_STRING
	la	$a0, newline
	syscall

	li	$v0, PRINT_STRING
	la	$a0, banner_middle
	syscall

	li	$v0, PRINT_STRING
	la	$a0, newline
	syscall

	li	$v0, PRINT_STRING
	la	$a0, banner_outline
	syscall

	li	$v0, PRINT_STRING
	la	$a0, newline
	syscall

	li	$v0, PRINT_STRING
	la	$a0, newline
	syscall
	
	lw	$ra, 0($sp)
	addi	$sp, $sp, 4
	jr	$ra

#-----------------------------

#
# Name:     read_input
# Description:  reads in stdin input to determine forest fire parameters
#		and stores it in the supplied struct
# Arguments:    a0:     address of the struct to store input with 4 values:
#			- a word to store supplied grid size
#			- a word to store supplied # of generations
#			- an asciiz to store supplied wind direction character
#			- the address of the array to store strings
#		a1:	maximum number of elements in the array
# Returns:      v0:	an integer representing if the grid size was erroneous
# Destroys:     t0, t1, t2, t3, t4, t5
#
read_input:
	addi	$sp, $sp, -12
	sw	$ra, 8($sp)
	sw	$s1, 4($sp)
	sw	$s0, 0($sp)

	move	$s0, $a0
	move	$s1, $a1

	move	$t0, $a0	# t0 is pointer to struct

	li	$v0, READ_INT  	# read in grid size
	syscall
	sw	$v0, 0($t0)
	move	$t1, $v0	# store grid size for reading in array
	addi	$t0, $t0, 4	# increment struct pointer

	li	$v0, READ_INT  	# read in # of generations
	syscall
	sw	$v0, 0($t0)
	addi	$t0, $t0, 4	# increment struct pointer

	li	$v0, READ_STRING
	la	$a0, fig_char
	li	$a1, 10		# buffer	
	syscall			# read in wind direction
	lb	$t2, 0($a0)
	sb	$t2, 0($t0)
	addi	$t0, $t0, 4	# increment struct pointer (to word boundry)

	li	$t2, -1		# init t2 to -1 (0 once starting loop)
	la	$t3, 0($t0)	# t3 points to string array

	slt	$t4, $s1, $t1	# t4 = 1 if grid size > max
	beq	$t4, $zero, read_grid
	move	$t1, $s1
	# prevents overflowing for grid sizes > max, but leaves error
	# checking to error_check, and just accepts first 900 bytes

read_grid:
	move	$t5, $t1
	addi	$t5, $t5, 1	# grid size + 1 for null terminator
	li	$t7, 32		# buffer long enough for max and null term

read_grid_loop:			# read in each line of characters
	addi	$t2, $t2, 1
	slt	$t4, $t2, $t1	# while t2 < max or grid size (lines)
	beq	$t4, $zero, read_done

	li	$v0, READ_STRING
	la	$a0, fig_char
	move	$a1, $t7	
	syscall			# read in grid size + 1 (1 line) chars
	li	$t6, 0

store_each_loop:		
	# store each char in line in array
	slt	$t4, $t6, $t1	# t6 < grid size (ignore last null byte)
	beq	$t4, $zero, read_grid_loop
	
	lb	$t4, 0($a0)	# load in a character into t4
	sb	$t4, 0($t3)	# store t4 to array

	addi	$t3, $t3, 1	# increment array ptr
	addi	$t6, $t6, 1	# incrememnt loop counter
	addi	$a0, $a0, 1	# increment read in string
	j	store_each_loop

read_done:
	move	$a0, $s0	# restore a0 to addr of struct
	move	$a1, $s1	# restore a1 to max elements
	lw	$ra, 8($sp)
	lw	$s1, 4($sp)
	lw	$s0, 0($sp)
	addi	$sp, $sp, 12
	jr	$ra	

	
#-----------------------------

#
# Name:     error_check
# Description:  runs through inputs to check and print errors if they occur
# Arguments:    a0:     a parameter block (struct) containing 4 values 
#                       - the supplied grid size
#                       - the supplied # of generations
#                       - the supplied wind direction
#                       - the address of the array of strings representing
#			  the grid
# Returns:      v0:	an integer representing whether errors were found.
#			(0, no errors; 1, errors)
# Destroys:     t1, t2, t3, t4, t5
#
error_check:
	addi	$sp, $sp, -12
	sw	$ra, 8($sp)
	sw	$s0, 4($sp)
	sw	$s1, 0($sp)

	li	$v0, 0		# will be 0 at func end if no errors
	move	$s0, $a0	# store a0

	lw	$t0, 0($a0)	# t0 = grid size val
	lw	$t1, 4($a0)	# t1 = gen # val
	lb	$t2, 8($a0)	# t2 = wind direction val
	
	li	$t4, GRID_MIN

	slt	$t3, $t0, $t4
	bne	$t3, $zero, grid_err	# grid size < 4
	li	$t4, GRID_MAX

	slt	$t3, $t4, $t0
	bne	$t3, $zero, grid_err	# grid size > 30

	slt	$t3, $t1, $zero
	bne	$t3, $zero, gen_err	# gen < 0
	li	$t4, GEN_MAX

	slt	$t3, $t4, $t1
	bne	$t3, $zero, gen_err	# gen > 30

	mul	$t3, $t0, $t0		# grid size^2 is num of bytes
	li	$t1, 0			# init t1 to 0
	la	$s1, 12($a0)		# s1 is ptr to array

grid_check_loop:
	slt	$t4, $t1, $t3		# while t1 < t3
	beq	$t4, $zero, err_check_cont

	lb	$t5, 0($s1)		# t5 is char to check
	
	li	$t4, 46			# load '.'
	beq	$t5, $t4, incr_check_loop	

	li	$t4, 66			# load 'B'
	beq	$t5, $t4, incr_check_loop

	li	$t4, 116		# load 't'
	beq	$t5, $t4, incr_check_loop

	j	bad_char_err	# only reached if char invalid

incr_check_loop:
	addi	$t1, $t1, 1	# incr loop count
	addi	$s1, $s1, 1	# incr array ptr 
	j	grid_check_loop
	
err_check_cont:
	li	$t3, 78			# load 'N'
	beq	$t3, $t2, check_done	# valid if N

	li	$t3, 83			# load 'S'
	beq	$t3, $t2, check_done	# valid if S

	li	$t3, 69			# load 'E'
	beq	$t3, $t2, check_done	# valid if E

	li	$t3, 87			# load 'W'
	beq	$t3, $t2, check_done	# valid if W

	j	wind_err		# invalid otherwise

grid_err:
	
	li	$v0, PRINT_STRING
	la	$a0, grid_error_str
	j	err_done

gen_err:
	li	$v0, PRINT_STRING
	la	$a0, gen_error_str
	j	err_done

wind_err:
	li	$v0, PRINT_STRING
	la	$a0, wind_error_str
	j	err_done

bad_char_err:
	li	$v0, PRINT_STRING
	la	$a0, bad_char_error_str
	j	err_done

err_done:
	syscall
	li	$v0, 1
	j	check_done	

check_done:
	move	$a0, $s0	# restore a0
	lw	$s1, 0($sp)
	lw	$s0, 4($sp)
	lw	$ra, 8($sp)
	addi	$sp, $sp, 12
	jr	$ra
