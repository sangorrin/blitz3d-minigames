;Juego tetris version1 : 3-Agosto-2015
;Daniel Sangorrin Lopez
;---------------------------------------------------------------------------------------------------

;modo grafico
HidePointer 
Graphics3D 800,600,16,2
SetBuffer BackBuffer()
AppTitle " BlitzTetris - Daniel Sangorrin, 2015"
SeedRnd MilliSecs()

;constantes
Const key_left=203
Const key_right=205
Const key_down=208
Const key_space=57

;---------------------------------------------------------------------------------------------------
;-------------------------------  pantalla inicial, empieza el juego  ------------------------------
;---------------------------------------------------------------------------------------------------

portada=LoadImage("portada.jpg")
cursor=LoadImage("pointer.bmp")
cursor2=LoadImage("pointer2.bmp")
MoveMouse 400,300
Repeat 
	Cls 
	If KeyDown(1) End
	DrawImage portada,0,0
	If (ImageRectCollide (cursor,MouseX(),MouseY(),0,330,540,130,30) Or ImageRectCollide (cursor2,MouseX(),MouseY(),0,330,540,130,30)) Then
			DrawImage cursor2,MouseX(),MouseY()
			If MouseDown(1) Then
				Exit
			End If 		
	Else 
			DrawImage cursor,MouseX(),MouseY()
	End If 
	If KeyDown(28) Exit 
	Flip
Forever
FreeImage portada
FreeImage cursor
FreeImage cursor2

;---------------------------------------------------------------------------------------------------
;------------------------------------   CARGA DEL JUEGO   ------------------------------------------
;---------------------------------------------------------------------------------------------------
;										globales
;---------------------------------------------------------------------------------------------------
Global luz,camera,cuadro,fondo,marco1,pivotpieza
Global tipo%,tipoold%,lineas%,nivelactual%,velocidadactual%,l_nec_actual% 
;(nota: Las variables no inicializadas toman por defecto valor 0)

Global brush_apagado=CreateBrush (200,200,200)
	   BrushShininess brush_apagado,0.5
	   BrushAlpha brush_apagado,0.8
	   BrushColor brush_apagado,180,20,20
Global brush_encendido=CreateBrush (255,255,255)
	   BrushShininess brush_encendido,1
	   

Global fuente=LoadFont("avquest",30,True,False,False)
SetFont fuente
Color 150,30,80

;											types
;---------------------------------------------------------------------------------------------------
;parametros de cada nivel
Type tiponivel
	Field velocidad%,lineasnecesarias%
End Type 
Dim nivel.tiponivel(5) ;hay cinco niveles empezando por el nivelactual=0
For i=0 To 4
	nivel(i)=New tiponivel
	nivel(i)\velocidad=1000-i*200
	nivel(i)\lineasnecesarias=5+i*3
Next 

;Matrices pieza,pieza siguiente y buffer del escenario
Dim celdas%(3,3)  ;matriz de 4x4 para las piezas
Type tipopieza
	Field i%,j%   ;posicion de la celda 0,0 dentro de el buffer general
End Type 
pieza.tipopieza=New tipopieza

Type tipobuffer
	Field valor%      ;1 si se muestra la imagen y 0 si no
	Field cuadro	 ;entidad cuadro q se mostrara o no segun el valor de 1
End Type
cuadro=LoadMesh("cubo.3ds")
ScaleEntity cuadro,.247,.247,.247
HideEntity cuadro

;buffer escenario
Dim buffer.tipobuffer(23,19)
For i=0 To 23
	For j=0 To 19
		buffer(i,j)=New tipobuffer
		buffer(i,j)\valor=1
	Next
Next
For i=0 To 19
	For j=4 To 15
		buffer(i,j)\valor=0
		buffer(i,j)\cuadro=CopyEntity(cuadro)
		PositionEntity buffer(i,j)\cuadro,j-10,-i+12,0
		HideEntity buffer(i,j)\cuadro
	Next
Next
;pieza siguiente
Dim celdas_sig.tipobuffer(3,3)
pivotpieza=CreatePivot()
MoveEntity pivotpieza,13,8,0
For i=0 To 3
	For j=0 To 3
			celdas_sig(i,j)=New tipobuffer
			celdas_sig(i,j)\valor=0
			celdas_sig(i,j)\cuadro=CopyEntity(cuadro)
			PositionEntity celdas_sig(i,j)\cuadro,j+13,-i+10,0
			HideEntity celdas_sig(i,j)\cuadro
	Next
Next
For i=0 To 3
	For j=0 To 3
		EntityParent celdas_sig(i,j)\cuadro,pivotpieza,True 
	Next
Next
MoveEntity pivotpieza,-.5,-1.35,0



;				creo el entorno, camara, luces, escenario, etc ...
;---------------------------------------------------------------------------------------------------
crear_entorno() ;esto pone el fondo,camara, luces y el marco (todavia no esta flipeado)
tipo%=Rnd(1,7)  ;antes de empezar preparo la q será la primera pieza
llena_celdas()

;---------------------------------------------------------------------------------------------------
;------------------------------------------  MAIN LOOP  --------------------------------------------
;---------------------------------------------------------------------------------------------------
Repeat 
	Cls
	;Pantalla inicial de cada nivel
	velocidadactual=nivel(nivelactual)\velocidad
	l_nec_actual=nivel(nivelactual)\lineasnecesarias
	nivelimage=LoadImage("nivel"+nivelactual+".jpg")
	DrawImage nivelimage,0,0
	Flip
	Delay 2000
	FreeImage nivelimage 
	FlushKeys()	
	;Bucle del juego en sí	
	Repeat
		If KeyDown(1) End 
		genera_pieza(pieza.tipopieza)
		oldtime=MilliSecs()
		
		;tras generar una pieza hay un ciclo hasta q se deposita (ver EXIT)
		Repeat
			Cls 
			time=MilliSecs()
			If time>oldtime+velocidadactual Then
				borrapieza(pieza.tipopieza)
				pieza\i=pieza\i+1
				oldtime=time
		    	If ilegal(pieza.tipopieza) Then
					pieza\i=pieza\i-1
					For n=0 To 3
						For m=0 To 3
							If celdas%(n,m)=1 Then 
								buffer(pieza\i+n,pieza\j+m)\valor=1
								PaintEntity buffer(pieza\i+n,pieza\j+m)\cuadro,brush_apagado
							End If 
						Next
					Next
					linea()
					Exit ;(EXIT)
				End If 
			End If 
			
			game_input(pieza.tipopieza)
			Actualizar(pieza.tipopieza)
	   		UpdateWorld
   			RenderWorld
 
			Text 202,55,lineas,True,False 
			Text 670,453,nivelactual,False,False 
			Text 700,495,l_nec_actual,False,False 
			Flip
		Forever
		
		
		
		If lineas>=l_nec_actual Then   ;si hemos hecho más lineas de las necesarias para el nivel 
			lineas=0				   ;es ke lo hemos superado y nos salimos a recoger el premio
			Exit
		End If 
		
	Forever 
	
	premio()						    ;cogemos el premio 
	If nivelactual=4 End				;si es el premio del nivel4 es ke nos hemos pasado el juego
	nivelactual=nivelactual+1			;si no nos vamos a jugar el siguiente nivel	
Forever
End 


;---------------------------------------------------------------------------------------------------
;------------------------------------------  FUNCIONES  --------------------------------------------
;---------------------------------------------------------------------------------------------------
Function crear_entorno()
	;luz
	luz=150
    AmbientLight luz,luz,luz
	Sol=CreateLight(3)
	PositionEntity sol,0,0,-200
	PointEntity sol,cuadro
	LightColor Sol,25,25,25
	;camara
	camera=CreateCamera()
	PositionEntity camera,0,2,-23
	CameraClsColor camera,150,150,150
	;fondo
	fondo=LoadSprite("colinasazules.jpg",1)
	SpriteViewMode fondo,2
	ScaleSprite fondo,85,80
	PositionEntity fondo,0,3,70
	;marco
	marco1=LoadMesh("marco4.3ds")
	;cartel lineas
	cartellineas=LoadSprite("cartellineas.png",2)
	ScaleSprite cartellineas,1.7,1.3
	PositionEntity cartellineas,-3.3,5.5,-18
	;bandeja
	bandeja=LoadSprite("bandeja.png",2)
	ScaleSprite bandeja,2.2,1.4
	PositionEntity bandeja,3,-.5,-18
	;barto
	barto=LoadSprite("grafiti.png",2)
	SpriteViewMode barto,2
	ScaleSprite barto,5,5
	PositionEntity barto,-15,4,-1.6
	
End Function
;---------------------------------------------------------------------------------------------------
Function genera_pieza(pieza.tipopieza)
	;copiamos la pieza preparada a la pieza nueva
	For i=0 To 3
		For j=0 To 3
			celdas(i,j)=celdas_sig(i,j)\valor
		Next
	Next
	
	;la colocamos en la salida
	pieza\i=0
	pieza\j=8
	
	tipoold%=tipo%
	;creamos la pieza siguiente
	tipo%=Rnd(1,7)
	brush(tipo%)		;preparo el brush para la pieza siguiente
	borraceldas(0)		;ponemos a ceros la matriz de la pieza siguiente
	llena_celdas()		;ponemos los unos en la matriz de la pieza siguiente segun tipo%
	For i=0 To 3
		For j=0 To 3
			If celdas_sig(i,j)\valor=1 Then 
				ShowEntity celdas_sig(i,j)\cuadro
				PaintEntity celdas_sig(i,j)\cuadro,brush_encendido 
			Else 
				HideEntity celdas_sig(i,j)\cuadro
			End If 
		Next
	Next
	Brush(tipoold%)		;vuelvo a poner el brush de la pieza actual
	If ilegal(pieza.tipopieza) Then 
		UpdateWorld
		RenderWorld
		Text 202,55,lineas,True,False 
		Text 670,453,nivelactual,False,False 
		Text 700,495,l_nec_actual,False,False 
		Flip
		Delay 1000
		GameOver() 
	End If 
	
End Function
;---------------------------------------------------------------------------------------------------
Function actualizar(pieza.tipopieza)
	;mostramos y ocultamos los cuadros apagados
	For n=0 To 19
		For m=4 To 15
			If buffer(n,m)\valor=1 Then 
				ShowEntity buffer(n,m)\cuadro
				PaintEntity buffer(n,m)\cuadro,brush_apagado
			Else
				HideEntity buffer(n,m)\cuadro
			End If
		Next
	Next
	;escribimos la nueva posicion
	For n=0 To 3
		For m=0 To 3
			If celdas%(n,m)=1 Then 
				buffer(pieza\i+n,pieza\j+m)\valor=1
				ShowEntity buffer(pieza\i+n,pieza\j+m)\cuadro
				PaintEntity buffer(pieza\i+n,pieza\j+m)\cuadro,brush_encendido
			End If 
		Next
	Next
	
	TurnEntity pivotpieza,-1,1,2
	
End Function 
;---------------------------------------------------------------------------------------------------
Function borrapieza(pieza.tipopieza)
			For n=0 To 3
				For m=0 To 3
					If celdas%(n,m)=1 Then buffer(pieza\i+n,pieza\j+m)\valor=0
				Next
			Next
End Function 
;---------------------------------------------------------------------------------------------------
Function ilegal(pieza.tipopieza)
			For n=0 To 3
				For m=0 To 3
					If celdas%(n,m)+buffer(pieza\i+n,pieza\j+m)\valor=2 Return True 
				Next
			Next
End Function 
;---------------------------------------------------------------------------------------------------
Function llena_celdas()
	Select tipo%
		Case 1
			celdas_sig(2,1)\valor=1
			celdas_sig(2,2)\valor=1   ;		 __
			celdas_sig(3,1)\valor=1	;	    |__|
			celdas_sig(3,2)\valor=1
		Case 2
			celdas_sig(3,0)\valor=1
			celdas_sig(3,1)\valor=1	;	   __
			celdas_sig(2,1)\valor=1	;	__|
			celdas_sig(2,2)\valor=1
		Case 3
			celdas_sig(0,1)\valor=1
			celdas_sig(1,1)\valor=1	;	palo vertical
			celdas_sig(2,1)\valor=1
			celdas_sig(3,1)\valor=1
		Case 4
			celdas_sig(2,1)\valor=1
			celdas_sig(2,2)\valor=1	;  __
			celdas_sig(3,2)\valor=1	;    |__
			celdas_sig(3,3)\valor=1
		Case 5
			celdas_sig(1,1)\valor=1
			celdas_sig(2,1)\valor=1	;	|
			celdas_sig(3,1)\valor=1	;	|--
			celdas_sig(2,2)\valor=1	;	|
		Case 6
			celdas_sig(1,1)\valor=1
			celdas_sig(1,2)\valor=1	; ___
			celdas_sig(2,2)\valor=1	; 	 |		
			celdas_sig(3,2)\valor=1	;    |
		Case 7
			celdas_sig(1,1)\valor=1
			celdas_sig(1,2)\valor=1	; ___
			celdas_sig(2,1)\valor=1	; |
			celdas_sig(3,1)\valor=1	; |


			
		Case 8
			celdas_sig(1,0)\valor=1
			celdas_sig(2,0)\valor=1	; |
			celdas_sig(2,1)\valor=1	; |__
			celdas_sig(3,1)\valor=1	;    |
		Case 9
			celdas_sig(1,0)\valor=1
			celdas_sig(1,1)\valor=1	
			celdas_sig(1,2)\valor=1	; palo horizontal
			celdas_sig(1,3)\valor=1	
		Case 10
			celdas_sig(1,3)\valor=1	;		|
			celdas_sig(2,3)\valor=1	;	  __|
			celdas_sig(2,2)\valor=1	;	 |
			celdas_sig(3,2)\valor=1
		Case 11
			celdas_sig(2,0)\valor=1
			celdas_sig(2,1)\valor=1	;		|
			celdas_sig(1,1)\valor=1	;	 ___|___
			celdas_sig(2,2)\valor=1
		Case 12
			celdas_sig(2,0)\valor=1
			celdas_sig(1,1)\valor=1	; 		|
			celdas_sig(2,1)\valor=1	;	  __|
			celdas_sig(3,1)\valor=1	;		|
		Case 13
			celdas_sig(2,0)\valor=1
			celdas_sig(2,1)\valor=1	;	__ __
			celdas_sig(2,2)\valor=1	;	  |	
			celdas_sig(3,1)\valor=1
		Case 14
			celdas_sig(1,2)\valor=1
			celdas_sig(2,0)\valor=1	; 
			celdas_sig(2,1)\valor=1	;     |
			celdas_sig(2,2)\valor=1	; ____|   
		Case 15
			celdas_sig(0,1)\valor=1
			celdas_sig(1,1)\valor=1	; |
			celdas_sig(2,1)\valor=1	; |
			celdas_sig(2,2)\valor=1	; |__
		Case 16
			celdas_sig(1,1)\valor=1
			celdas_sig(1,2)\valor=1	; ______
			celdas_sig(1,3)\valor=1	; |
			celdas_sig(2,1)\valor=1	; |
		Case 17
			celdas_sig(1,1)\valor=1
			celdas_sig(2,1)\valor=1	; |
			celdas_sig(2,2)\valor=1	; |
			celdas_sig(2,3)\valor=1	; |________
		Case 18
			celdas_sig(1,2)\valor=1
			celdas_sig(2,2)\valor=1	;   |
			celdas_sig(3,2)\valor=1	;   |
			celdas_sig(3,1)\valor=1	; __|
		Case 19
			celdas_sig(2,0)\valor=1
			celdas_sig(2,1)\valor=1	; _____
			celdas_sig(2,2)\valor=1	;     |
			celdas_sig(3,2)\valor=1	;     |
	End Select
End Function 
;---------------------------------------------------------------------------------------------------
Function borraceldas(a)
	If a=1 Then ;borramos la pieza actual
		For i=0 To 3
			For j=0 To 3
				celdas(i,j)=0
			Next
		Next
	Else	   ;borramos la pieza siguiente
		For i=0 To 3
			For j=0 To 3
				celdas_sig(i,j)\valor=0
			Next
		Next
	End If 
End Function 
;---------------------------------------------------------------------------------------------------
Function linea()
;con un bucle vamos comprobando las lineas de arriba abajo
	For n=0 To 19
		linea%=1
		For m=4 To 15
			If buffer(n,m)\valor=0 Then linea%=0
		Next
		If linea%=1 Then  ;si hay linea desplazamos lo de arriba pabajo
		    lineas%=lineas%+1
			For z=4 To 15
				HideEntity buffer(n,z)\cuadro
				TurnEntity pivotpieza,-1,1,2  ;este sigue a su bola claro
				UpdateWorld
				RenderWorld
				Text 202,55,lineas,True,False 
				Text 670,453,nivelactual,False,False 
				Text 700,495,l_nec_actual,False,False 
				Flip 
				Delay 20
			Next 
			
			For i=n To 1 Step -1
				For j=4 To 15
					buffer(i,j)\valor=buffer(i-1,j)\valor
				Next
			Next
			
		End If	
	Next	
;actualizamos la pantalla	
	For n=0 To 19
		For m=4 To 15
			If buffer(n,m)\valor=1 Then 
				ShowEntity buffer(n,m)\cuadro
				PaintEntity buffer(n,m)\cuadro,brush_apagado
			Else
				HideEntity buffer(n,m)\cuadro
			End If
		Next
	Next
	TurnEntity pivotpieza,-1,1,2
	UpdateWorld
	RenderWorld
	Text 202,55,lineas,True,False 
	Text 670,453,nivelactual,False,False 
	Text 700,495,l_nec_actual,False,False 
	Flip 
	
End Function 
;---------------------------------------------------------------------------------------------------
Function gameover()
	Cls
	Text 400,300,"GAME OVER,  Pulsa una tecla para salir",True,True 
	Flip
	FlushKeys()
	WaitKey()
	End 
End Function 
;---------------------------------------------------------------------------------------------------
Function premio()
	Cls 
	premioimage=LoadImage("premio"+nivelactual+".jpg")
	DrawImage premioimage,0,0
	Flip
	FlushKeys()
	WaitKey()
	FreeImage premioimage 

End Function 
;---------------------------------------------------------------------------------------------------
Function cambia_celdas()
	Select tipoold%
		Case 1
			celdas(2,1)=1
			celdas(2,2)=1   ;		 __
			celdas(3,1)=1	;	    |__|
			celdas(3,2)=1
		Case 2
			celdas(3,0)=1
			celdas(3,1)=1	;	   __
			celdas(2,1)=1	;	__|
			celdas(2,2)=1
		Case 3
			celdas(0,1)=1
			celdas(1,1)=1	;	palo vertical
			celdas(2,1)=1
			celdas(3,1)=1
		Case 4
			celdas(2,1)=1
			celdas(2,2)=1	;  __
			celdas(3,2)=1	;    |__
			celdas(3,3)=1
		Case 5
			celdas(1,1)=1
			celdas(2,1)=1	;	|
			celdas(3,1)=1	;	|--
			celdas(2,2)=1	;	|
		Case 6
			celdas(1,1)=1
			celdas(1,2)=1	; ___
			celdas(2,2)=1	; 	 |		
			celdas(3,2)=1	;    |
		Case 7
			celdas(1,1)=1
			celdas(1,2)=1	; ___
			celdas(2,1)=1	; |
			celdas(3,1)=1	; |

			
		Case 8
			celdas(1,0)=1
			celdas(2,0)=1	; |
			celdas(2,1)=1	; |__
			celdas(3,1)=1	;    |
		Case 9
			celdas(1,0)=1
			celdas(1,1)=1	
			celdas(1,2)=1	; palo horizontal
			celdas(1,3)=1	
		Case 10
			celdas(1,3)=1	;		|
			celdas(2,3)=1	;	  __|
			celdas(2,2)=1	;	 |
			celdas(3,2)=1
		Case 11
			celdas(2,0)=1
			celdas(2,1)=1	;		|
			celdas(1,1)=1	;	 ___|___
			celdas(2,2)=1
		Case 12
			celdas(2,0)=1
			celdas(1,1)=1	; 		|
			celdas(2,1)=1	;	  __|
			celdas(3,1)=1	;		|
		Case 13
			celdas(2,0)=1
			celdas(2,1)=1	;	__ __
			celdas(2,2)=1	;	  |	
			celdas(3,1)=1
		Case 14
			celdas(1,2)=1
			celdas(2,0)=1	; 
			celdas(2,1)=1	;     |
			celdas(2,2)=1	; ____|   
		Case 15
			celdas(0,1)=1
			celdas(1,1)=1	; |
			celdas(2,1)=1	; |
			celdas(2,2)=1	; |__
		Case 16
			celdas(1,1)=1
			celdas(1,2)=1	; ______
			celdas(1,3)=1	; |
			celdas(2,1)=1	; |
		Case 17
			celdas(1,1)=1
			celdas(2,1)=1	; |
			celdas(2,2)=1	; |
			celdas(2,3)=1	; |________
		Case 18
			celdas(1,2)=1
			celdas(2,2)=1	;   |
			celdas(3,2)=1	;   |
			celdas(3,1)=1	; __|
		Case 19
			celdas(2,0)=1
			celdas(2,1)=1	; _____
			celdas(2,2)=1	;     |
			celdas(3,2)=1	;     |
		
	End Select
End Function 
;---------------------------------------------------------------------------------------------------
Function game_input(pieza.tipopieza)	
			If KeyDown(1) End 
			;If KeyDown(44) MoveEntity camera,1,0,0:PointEntity camera,cuadro

			If KeyHit(key_down) Then
				borrapieza(pieza.tipopieza)
				Repeat 
					pieza\i=pieza\i+1
				Until ilegal(pieza.tipopieza) 
				pieza\i=pieza\i-1
			End If 
				
			If KeyHit(key_left) Then 
				borrapieza(pieza.tipopieza) 
				pieza\j=pieza\j-1
				If ilegal(pieza.tipopieza) pieza\j=pieza\j+1
			End If 
			If KeyHit(key_right) Then 
				borrapieza(pieza.tipopieza) 
				pieza\j=pieza\j+1
				If ilegal(pieza.tipopieza) pieza\j=pieza\j-1
			End If 
			If KeyHit(key_space) Then
				borrapieza(pieza.tipopieza)
				borraceldas(1)
				tipooldtemp=tipoold
				Select tipoold
				Case 2
					tipoold=8
				Case 8
					tipoold=2
				Case 3
					tipoold=9
				Case 9
					tipoold=3
				Case 4
					tipoold=10
				Case 10
					tipoold=4
				Case 5
					tipoold=11
				Case 11
					tipoold=12
				Case 12
					tipoold=13
				Case 13
					tipoold=5
				Case 6
					tipoold=14
				Case 14
					tipoold=15
				Case 15
					tipoold=16
				Case 16
					tipoold=6
				Case 7
					tipoold=17
				Case 17
					tipoold=18
				Case 18
					tipoold=19
				Case 19
					tipoold=7
								
				End Select 
				cambia_celdas()  ;similar a llena_celdas pero pa la pieza	
				If ilegal(pieza.tipopieza) Then borraceldas(1):tipoold=tipooldtemp:cambia_celdas()
			End If
End Function 
;---------------------------------------------------------------------------------------------------
Function brush(tipoqsea%)
	Select tipoqsea
		Case 1
			BrushColor brush_encendido,0,0,255
		Case 2
			BrushColor brush_encendido,0,255,0
		Case 3
			BrushColor brush_encendido,255,0,0
		Case 4
			BrushColor brush_encendido,0,255,0
		Case 5
			BrushColor brush_encendido,200,100,0
		Case 6
			BrushColor brush_encendido,250,150,200
		Case 7
			BrushColor brush_encendido,250,150,200
	End Select 
End Function 