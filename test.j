.class public Test
.super java/lang/Object
.field static temp I
.field static global_arr [I
.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method
.method public static arrprint([I[I)V
	.limit stack 32
	.limit locals 32
ldc 0
istore 2
label5:
iload 2 
ldc 3 
if_icmplt label0
ldc 1
goto label1
label0:
ldc 0
label1: 
ifne label6
getstatic java/lang/System/out Ljava/io/PrintStream; 
aload 0
iload 2 
iaload
invokevirtual java/io/PrintStream/println(I)V
iload 2 
ldc 1
iadd
istore 2
goto label5
label6:
return
.end method
.method public static main([Ljava/lang/String;)V
	.limit stack 32
	.limit locals 32
ldc 1
putstatic Test/temp I
ldc 3
newarray int
putstatic Test/global_arr [I
ldc 3
newarray int
astore 1
aload 1
ldc 0 
ldc 1 
iastore
aload 1
ldc 1 
ldc 2 
iastore
aload 1
ldc 2 
ldc 3 
iastore
getstatic Test/global_arr [I
ldc 0 
ldc 0 
iastore
getstatic Test/global_arr [I
ldc 1 
ldc 1 
iastore
getstatic Test/global_arr [I
ldc 2 
ldc 2 
iastore
getstatic Test/global_arr [I
ldc 0
getstatic Test/global_arr [I
ldc 0 
iaload
ldc 1
isub
iastore
getstatic Test/global_arr [I
aload 1 
invokestatic Test/arrprint([I[I)V
return
.end method
