package org.communityaid.model

enum EligibilityStatus(val label: String):
  case Priority extends EligibilityStatus("Priority")
  case Eligible extends EligibilityStatus("Eligible")
  case NotEligible extends EligibilityStatus("Not eligible")
