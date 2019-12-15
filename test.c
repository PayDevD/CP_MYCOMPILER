int temp = 1;
int global_arr[3];

void arrprint(int arr[], int arr2[]) {
int i = 0;
while(i < 3) {
_print(arr[i]);
++i;
}
}

int main() {
int arr[3];
int a;
arr[0] = 1;
arr[1] = 2;
arr[2] = 3;
global_arr[0] = 0;
global_arr[1] = 1;
global_arr[2] = 2;
a = global_arr[0];
arrprint(global_arr, arr);
}