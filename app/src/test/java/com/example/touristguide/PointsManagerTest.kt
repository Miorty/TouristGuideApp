package com.example.touristguide

import com.example.touristguide.core.gamification.PointsManager
import org.junit.Assert.assertEquals
import org.junit.Test

class PointsManagerTest {
    @Test fun levelIsCalculatedFromPoints() { assertEquals(2, PointsManager().calculateLevel(100)) }
}
