;Segunda demo
;Daniel Sangorrin Lopez
;Agosto 2015
;-----------------------------------------------------------------------------------------
HidePointer 
AppTitle "El avioncito"
Graphics3D 800,600,16,2
SetBuffer BackBuffer()
;-----------------------------------------------------------------------------------------
;Variables, constantes .......

Const ENTIDAD_TERRENO =   1
Const ENTIDAD_CAMARA  =   2
Const ENTIDAD_PIVOT   =   3
Const ENTIDAD_AVION	  =   4
Const ENTIDAD_GLOBO	  =   5
Const key_up=200
Const key_down=208
Const key_left=203
Const key_right=205
Const centrox=8610,centroz=7080

Global terreno,camara,avion,pivot
Global aceleraleft%=0,aceleraright%=0,aceleraup%=0,aceleradown%=0
Global alturamax%=1400
Global stepz=20,v_camara=1.5,vueltas%=0
Global indice%=0
Global cviejo=1
Global globosound=LoadSound("globo.wav")
Global destruccion=LoadSound("destruccion.wav")

Dim cuadrante%(3)
cuadrante(indice)=1

;Cargo terreno, avion, luces, camara ...
Crear_entorno()
;globitos
Global globo=LoadSprite("globo.png",2)
ScaleSprite globo,150,150
SpriteViewMode globo,1
PositionEntity globo,12690,806,6000
EntityRadius avion,50,50
EntityPickMode avion,1

;Colisiones
EntityType terreno,ENTIDAD_TERRENO	
EntityType camara,ENTIDAD_CAMARA
EntityType avion,ENTIDAD_AVION  
Collisions ENTIDAD_CAMARA,ENTIDAD_TERRENO,2,3
Collisions ENTIDAD_PIVOT,ENTIDAD_TERRENO,2,3
Collisions ENTIDAD_AVION,ENTIDAD_TERRENO,2,1
;-----------------------------------------------------------------------------------------
;-------------------------------        MAIN LOOP      -----------------------------------
;-----------------------------------------------------------------------------------------
Repeat 
	Cls
	If KeyDown(1) End
	mueve_avion()
	mueve_camara(pivot,v_camara)
	If Animating(avion)<>True Animate avion,1,2
	If EntityCollided(avion,ENTIDAD_TERRENO)<>False destruccion()
	If EntityPick(globo,120)=avion globo()
	UpdateWorld
	RenderWorld
	angulo#=angulo(centrox,centroz,EntityX(avion,True),EntityZ(avion,True))
	num_vueltas(que_cuadrante(angulo#))
	Text 30,30,"vueltas: "+vueltas,False,False 
	Flip
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
	terreno=LoadTerrain("terreno1.jpg")
	ScaleEntity terreno,30,1450,30
	TerrainShading terreno,1
	TerrainDetail terreno,2000,True
	; Texturizamos el terreno 
	textura1=LoadTexture("textura1.jpg")
	textura2=LoadTexture("textura2.jpg")
	PositionTexture textura1,0,0
	ScaleTexture textura1,512,512
	EntityTexture terreno,textura1,0,0
	PositionTexture textura2,0,0
	ScaleTexture textura2,128,128
	EntityTexture terreno,textura2,0,1
	
	;cargar el avion
	avion=LoadAnimMesh("avion.3ds")
	ScaleEntity avion,.5,.5,.5
	textavion=LoadTexture("textura_.jpg")
	EntityTexture avion,textavion
	pivot=CreatePivot()
	EntityParent avion,pivot
	PositionEntity pivot,12690,800,7200

    ; creando la cámara 
	camara= CreateCamera()
    CameraRange camara,1,20000
	PositionEntity camara,12690,806,7180
	PointEntity camara,pivot
	
	
End Function

;mueve_camara:
Function mueve_camara(Entidad,velocidad_camara);Control de seguimiento de la camara

    x#=EntityX(Entidad)    
  	y#=EntityY(Entidad)
    z#=EntityZ(Entidad)
 
   	cx#=EntityX(camara) 
	cz#=EntityZ(camara)
    objetivo_x#=EntityX(Entidad)
    objetivo_z#=EntityZ(Entidad)

    cx#=cx#+((objetivo_x#-cx#)/velocidad_camara*1.5) 
   	cz#=cz#+((objetivo_z#-cz#)/velocidad_camara*1.5)

       
    PositionEntity camara,cx,y+8,cz
    PointEntity camara,Entidad
	TurnEntity camara,-15,0,0

	
End Function 

;mueve_avion
Function mueve_avion()
	If KeyDown(key_left) Then
		aceleraright=0
		If EntityRoll(avion)<30 TurnEntity avion,0,0,1
		If aceleraleft%<102 aceleraleft%=aceleraleft%+3
		TurnEntity pivot,0,.03*aceleraleft,0
		
	EndIf 
	If KeyDown(key_right) Then
		aceleraleft=0
		If EntityRoll(avion)>-30 TurnEntity avion,0,0,-1
		If aceleraright%<102 aceleraright%=aceleraright%+3
		TurnEntity pivot,0,-.03*aceleraright,0
	EndIf 
	If KeyDown(key_up) Then
		If EntityPitch(avion)<20 TurnEntity avion,4,0,0
		If aceleradown%<102 aceleradown%=aceleradown%+6
		MoveEntity pivot,0,-.15*aceleradown,0
	EndIf
	If KeyDown(key_down) Then
		If EntityPitch(avion)>-20 TurnEntity avion,-4,0,0
			If aceleraup%<102 aceleraup%=aceleraup%+6
			If EntityY(avion,True)<alturamax MoveEntity pivot,0,.15*aceleraup,0
	EndIf 
	
	If Not (KeyDown(key_up) Or KeyDown(key_down)) Then
		av_pitch#=EntityPitch(avion)
		If Int(av_pitch)<>0	Then 
			RotateEntity avion,Int(av_pitch)-signo(av_pitch),0,EntityRoll(avion)
		Else
			RotateEntity avion,0,0,EntityRoll(avion)
		EndIf
		If aceleradown>0 aceleradown=aceleradown-2 MoveEntity pivot,0,-.15*aceleradown,0
		If (aceleraup>0 And EntityY(avion,True)<alturamax-50) aceleraup=aceleraup-2 MoveEntity pivot,0,.15*aceleraup,0
	EndIf	
	
	If Not (KeyDown(key_left) Or KeyDown(key_right)) Then
		av_roll#=EntityRoll(avion)
		If Int(av_roll)<>0 Then
			RotateEntity avion,EntityPitch(avion),0,Int(av_roll)-signo(av_roll)
		Else 
			RotateEntity avion,EntityPitch(avion),0,0
		EndIf 
		If aceleraleft>0 aceleraleft%=aceleraleft-2 TurnEntity pivot,0,.03*aceleraleft,0
		If aceleraright>0 aceleraright%=aceleraright-2 TurnEntity pivot,0,-.03*aceleraright,0
		If Not (aceleraleft>0 Or aceleraright>0) Then aceleraleft=0 :aceleraright=0
	EndIf 
	
	MoveEntity pivot,0,0,stepz 

	
End Function 
;funcion signo
Function signo(numero)
	signo=numero/Abs(numero)
	Return signo
End Function
;funcion destruccion
Function destruccion()
		PlaySound destruccion
		ClearCollisions 
		x=EntityX(avion,True)
		y=EntityY(avion,True)
		z=EntityZ(avion,True)
		angulo=0
		radio=20
		Repeat 
			PositionEntity camara,x+radio*Sin(angulo),y+8,z+radio*Cos(angulo)
			PointEntity camara,avion
			angulo=angulo+1
			If Int(angulo)=360 Then angulo=0
			UpdateWorld
			RenderWorld
			Flip
		Until KeyDown(1)
		End
End Function 	
;angulo de un punto respecto a un punto que hace de pivote
Function angulo(pivx,pivz,x2,z2)
longx#=x2-pivx
longz#=z2-pivz
angulo#=(longx#<0)*180+ATan(longz#/longx#)
angulo#=angulo#+360*(angulo#<0)
Return angulo#
End Function 
;funcion q nos dice en cual de los 3 cuadrantes esta el angulo
Function que_cuadrante(angulo#)
If angulo>=0 And angulo<90 Return 1
If angulo>=90 And angulo<270 Return 2
If angulo>=270 And angulo<=360 Return 3
End Function
;funcion q se encarga de mantener el estado de la carrera y devuelve el numero de vueltas que lleva
Function num_vueltas(cuadro%)
	a%=cuadro-cviejo
	b%=cuadro-cuadrante(indice)
	
	If (a%=1 And b%=1) Then ;se ha pasado al siguiente cuadrante (de 1 a 2 o de 2 a 3 )
		indice%=indice%+1
		cuadrante(indice)=cuadro
	EndIf
	If (a%=-2 And b%=-2) Then ;se ha dado una vuelta pues se ha pasado de 3 a 1
		indice=indice+1
		If indice=3 Then indice=0
		cuadrante(indice)=cuadro
		vueltas%=vueltas+1
	EndIf
	
cviejo=cuadro
End Function 
;funcion para cuando coje un globo
Function globo()
	PlaySound globosound
	HideEntity globo
End Function 