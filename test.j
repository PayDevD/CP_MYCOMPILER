.class public Test
.super java/lang/Object
.method public <init>()V
aload_0
invokenonvirtual java/lang/Object/<init>()V
return
.end method
.method public static add(II)I
	.limit stack 32
	.limit locals 32
iload 0 
iload 1 
iadd 
istore_2 
iload 2
ireturn
.end method
.method public static main([Ljava/lang/String;)V
	.limit stack 32
	.limit locals 32
ldc 25
istore 1
ldc 0
istore 2
label18:
iload 1 
ldc 10 
if_icmplt label0
ldc 1
goto label1
label0:
ldc 0
label1: 
ifeq label3
label2:
ldc 0
goto label4
label3:
ldc 1
label4: 
iload 2 
ifeq label5
ifeq label7
ldc 1
goto label6
label5:
pop
label7:
ldc 0
label6: 
ifne label19
iload 1 
ldc 1
isub
istore 1
iload 1 
ldc 10 
if_icmpge label11
ldc 1
goto label12
label11:
ldc 0
label12 : 
ifne label17
getstatic java/lang/System/out Ljava/io/PrintStream; 
iload 1 
invokevirtual java/io/PrintStream/println(I)V
ldc 1 
istore_2 
goto label16
label17:
getstatic java/lang/System/out Ljava/io/PrintStream; 
ldc 1 
ineg 
invokevirtual java/io/PrintStream/println(I)V
label16:
goto label18
label19:
getstatic java/lang/System/out Ljava/io/PrintStream; 
ldc 1 
iload 1 
invokestatic Test/add(II)I
invokevirtual java/io/PrintStream/println(I)V
return
.end method
