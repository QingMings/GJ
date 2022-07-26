import sys
from time import sleep

num = int(sys.argv[1])

def func_sum( num ):
    sum = 0
    for i in range(num+1):
        sum +=i
        print("i=%d, sum=%d"%(i, sum))
        sleep(1)
    return sum

func_sum( num )

print("sum=", sum)
