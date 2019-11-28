int add(int x, int y) {
	int z ;
	z = x+y;
	return z;
}

void main () {
	int t = 25;
	int a = 0;
while( !(t < 10) or a) {
--t;
if(t >= 10){
_print(t);
a = 1;
}else {
_print(-1);
}
}
_print(add(1,t));
}
