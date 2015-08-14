;Primera demo
;Daniel Sangorrin Lopez
;Agosto 2015
;-----------------------------------------------------------------------------------------
HidePointer 
AppTitle "Bolabicho"
Graphics3D 800,600,16,2
SetBuffer BackBuffer()
;-----------------------------------------------------------------------------------------
;Variables, constantes .......

Const ENTIDAD_TERRENO =   1
Const ENTIDAD_BICHO   =   2
Const key_up=200
Const key_down=208
Const key_left=203
Const key_right=205
Const FPS=30
Global tiempo_espera ;el tiempo antes de empezar los frames del muñeco en espera

Global terreno,camara,bicho,pivot
;-----------------------------------------------------------------------------------------
; Variables
	Global PLAYER = 1
	Global GROUNDFLOOR = 2
	Global plane
	
; Plane
	plane = CreatePlane()
	plane_tex = LoadTexture("grid.jpg")
	ScaleTexture plane_tex,15,15
	EntityTexture plane,plane_tex
	EntityAlpha plane,0.7
	EntityType plane,GROUNDFLOOR,1
	
	mirror = CreateMirror()
	
;-----------------------------------------------------------------------------------------

Crear_entorno()

;Colisiones
;EntityType terreno,ENTIDAD_TERRENO	
EntityType pivot,ENTIDAD_BICHO
Collisions ENTIDAD_BICHO,GROUNDFLOOR,2,3

;-----------------------------------------------------------------------------------------
;-------------------------------        MAIN LOOP      -----------------------------------
;-----------------------------------------------------------------------------------------
periodo=1000/FPS
tiempo=MilliSecs()-periodo

Repeat 
	Repeat
		transcurrido=MilliSecs()-tiempo
	Until transcurrido
	;Cuantos 'frames' tiene instante
	instante=transcurrido/periodo
	;fraccion del restante
	tween#=Float(transcurrido Mod periodo)/Float(periodo)

	
For k=1 To instante
	tiempo=tiempo+periodo
	If k=instante Then CaptureWorld

	If KeyDown(1) End
	mueve_bicho()
	mueve_camara(pivot,30)
	UpdateWorld
		;fps por segundo
 		Framecounter_counter=Framecounter_counter+1
 		If Framecounter_time+1001 <MilliSecs() Then
 			Framecounter_framerate=Framecounter_counter
 			Framecounter_counter=0
 			Framecounter_time=MilliSecs()
 		End If
 		

Next ; del instante de los frames

;Text 10,10,"fps: "+Framecounter_framerate
Flip
	RenderWorld tween; Dibujamos el mundo

Forever
;-----------------------------------------------------------------------------------------
;-----------------------------------------------------------------------------------------
;Crear_entorno:
Function Crear_entorno()
	;luces
	luz#=254
	AmbientLight luz#,luz,luz
    light=CreateLight()
	RotateEntity light,45,0,0
	LightColor light,200,200,200
	
    ;Cargar terreno 
	;terreno=LoadTerrain("terreno1.jpg")
	;ScaleEntity terreno,30,550,30
	;TerrainShading terreno,1
	;TerrainDetail terreno,2000,True
	; Texturizamos el terreno 
	;textura1=LoadTexture("textura1.jpg")
	;textura2=LoadTexture("textura2.jpg")
	;PositionTexture textura1,0,0
	;ScaleTexture textura1,512,512
	;EntityTexture terreno,textura1,0,0
	;PositionTexture textura2,0,0
	;ScaleTexture textura2,128,128
	;EntityTexture terreno,textura2,0,1
	
	;cargar el bicho
	bicho=LoadAnimMesh("bolabicho.b3d")
	pivot=CreatePivot()
	EntityParent bicho,pivot,True 
	ScaleEntity bicho,.1,.1,.1
	PositionEntity pivot,0,5,0
	TurnEntity bicho,0,180,0
	
    ; creando la cámara 
	camara= CreateCamera()
    CameraRange camara,1,20000
	PositionEntity camara,0,20,-15
End Function

;mueve_camara:
Function mueve_camara(Entidad,velocidad_camara);Control de seguimiento de la camara

    x#=EntityX(Entidad)    
  	y#=EntityY(Entidad)
    z#=EntityZ(Entidad)-10
 
   	cx#=EntityX(camara) 
	cz#=EntityZ(camara)
    objetivo_x#=EntityX(Entidad)
    objetivo_z#=EntityZ(Entidad)

    cx#=cx#+((objetivo_x#-cx#)/velocidad_camara) 
   	cz#=cz#+((objetivo_z#-cz#)/velocidad_camara)

       
    PositionEntity camara,cx,y+16,cz
    PointEntity camara,Entidad
	TurnEntity camara,-30,0,0

	
End Function 

;mueve_avion
Function mueve_bicho()
	If KeyDown(key_left) Then
		TurnEntity pivot,0,1,0
		RotateEntity bicho,0,-140,0
		
	EndIf 
	If KeyDown(key_right) Then
		TurnEntity pivot,0,-1,0
		RotateEntity bicho,0,140,0
	
	EndIf 
	If KeyDown(key_up) Then
		
	EndIf
	If KeyDown(key_down) Then
	
	EndIf 
	
	MoveEntity pivot,0,-.1,.8 ;gravedad + movimiento
	If Animating(bicho)=0 Then Animate bicho,1,.5	
		
End Function 