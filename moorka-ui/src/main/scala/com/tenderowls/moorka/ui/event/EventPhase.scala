package com.tenderowls.moorka.ui.event

sealed trait EventPhase

case object Idle extends EventPhase

case object Capturing extends EventPhase

case object AtTarget extends EventPhase

case object Bubbling extends EventPhase
