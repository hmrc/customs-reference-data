package services

import java.time.LocalDateTime

import javax.inject.Singleton

@Singleton
class TimeService {
  def now(): LocalDateTime = LocalDateTime.now()
}
