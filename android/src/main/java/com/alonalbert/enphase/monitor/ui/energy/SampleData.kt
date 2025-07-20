package com.alonalbert.enphase.monitor.ui.energy

import com.alonalbert.solar.combiner.enphase.model.DailyEnergy
import com.alonalbert.solar.combiner.enphase.model.Energy
import java.time.LocalDate

object SampleData {
  val sampleData = DailyEnergy(
    LocalDate.now().atStartOfDay().toLocalDate(),
    energies = listOf(
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.756,
        charged = 0.04,
        discharged = 0.004,
        mainExported = 0.0,
        imported = 0.792,
        battery = 62
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.808,
        charged = 0.028,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.836,
        battery = 56
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.748,
        charged = 0.028,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.776,
        battery = 56
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.016,
        charged = 0.032,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.048,
        battery = 56
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.844,
        charged = 0.036,
        discharged = 0.008,
        mainExported = 0.0,
        imported = 1.872,
        battery = 48
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.512,
        charged = 0.004,
        discharged = 0.404,
        mainExported = 0.0,
        imported = 0.112,
        battery = 55
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.576,
        charged = 0.008,
        discharged = 0.536,
        mainExported = 0.0,
        imported = 0.048,
        battery = 54
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.6,
        charged = 0.012,
        discharged = 0.58,
        mainExported = 0.0,
        imported = 0.032,
        battery = 53
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.6,
        charged = 0.012,
        discharged = 0.576,
        mainExported = 0.0,
        imported = 0.036,
        battery = 58
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.708,
        charged = 0.004,
        discharged = 0.668,
        mainExported = 0.0,
        imported = 0.044,
        battery = 58
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.748,
        charged = 0.0,
        discharged = 0.716,
        mainExported = 0.0,
        imported = 0.032,
        battery = 56
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.684,
        charged = 0.0,
        discharged = 0.664,
        mainExported = 0.0,
        imported = 0.02,
        battery = 56
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.692,
        charged = 0.0,
        discharged = 0.656,
        mainExported = 0.0,
        imported = 0.036,
        battery = 55
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.788,
        charged = 0.0,
        discharged = 0.736,
        mainExported = 0.0,
        imported = 0.052,
        battery = 54
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.768,
        charged = 0.0,
        discharged = 0.744,
        mainExported = 0.0,
        imported = 0.024,
        battery = 53
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.704,
        charged = 0.0,
        discharged = 0.668,
        mainExported = 0.0,
        imported = 0.036,
        battery = 52
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.68,
        charged = 0.0,
        discharged = 0.636,
        mainExported = 0.0,
        imported = 0.044,
        battery = 51
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.704,
        charged = 0.0,
        discharged = 0.68,
        mainExported = 0.0,
        imported = 0.024,
        battery = 50
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.796,
        charged = 0.0,
        discharged = 0.76,
        mainExported = 0.0,
        imported = 0.036,
        battery = 49
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.764,
        charged = 0.0,
        discharged = 0.732,
        mainExported = 0.0,
        imported = 0.032,
        battery = 48
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.644,
        charged = 0.0,
        discharged = 0.616,
        mainExported = 0.0,
        imported = 0.028,
        battery = 47
      ),
      Energy(
        exportProduced = 0.004,
        mainProduced = 0.0,
        consumed = 0.676,
        charged = 0.0,
        discharged = 0.632,
        mainExported = 0.0,
        imported = 0.044,
        battery = 46
      ),
      Energy(
        exportProduced = 0.004,
        mainProduced = 0.0,
        consumed = 0.872,
        charged = 0.0,
        discharged = 0.832,
        mainExported = 0.0,
        imported = 0.04,
        battery = 45
      ),
      Energy(
        exportProduced = 0.056,
        mainProduced = 0.0,
        consumed = 0.932,
        charged = 0.0,
        discharged = 0.88,
        mainExported = 0.0,
        imported = 0.052,
        battery = 44
      ),
      Energy(
        exportProduced = 0.076,
        mainProduced = 0.0,
        consumed = 0.916,
        charged = 0.0,
        discharged = 0.864,
        mainExported = 0.0,
        imported = 0.048,
        battery = 43
      ),
      Energy(
        exportProduced = 0.08,
        mainProduced = 0.004,
        consumed = 0.764,
        charged = 0.008,
        discharged = 0.704,
        mainExported = 0.0,
        imported = 0.064,
        battery = 42
      ),
      Energy(
        exportProduced = 0.128,
        mainProduced = 0.012,
        consumed = 0.644,
        charged = 0.008,
        discharged = 0.58,
        mainExported = 0.0,
        imported = 0.06,
        battery = 41
      ),
      Energy(
        exportProduced = 0.156,
        mainProduced = 0.024,
        consumed = 0.664,
        charged = 0.004,
        discharged = 0.572,
        mainExported = 0.0,
        imported = 0.068,
        battery = 40
      ),
      Energy(
        exportProduced = 0.176,
        mainProduced = 0.036,
        consumed = 0.72,
        charged = 0.008,
        discharged = 0.612,
        mainExported = 0.0,
        imported = 0.08,
        battery = 40
      ),
      Energy(
        exportProduced = 0.312,
        mainProduced = 0.072,
        consumed = 0.908,
        charged = 0.004,
        discharged = 0.768,
        mainExported = 0.0,
        imported = 0.068,
        battery = 39
      ),
      Energy(
        exportProduced = 0.436,
        mainProduced = 0.124,
        consumed = 2.336,
        charged = 0.036,
        discharged = 2.168,
        mainExported = 0.0,
        imported = 0.08,
        battery = 36
      ),
      Energy(
        exportProduced = 0.76,
        mainProduced = 0.212,
        consumed = 0.796,
        charged = 0.052,
        discharged = 0.572,
        mainExported = 0.0,
        imported = 0.064,
        battery = 35
      ),
      Energy(
        exportProduced = 1.316,
        mainProduced = 0.468,
        consumed = 0.832,
        charged = 0.072,
        discharged = 0.344,
        mainExported = 0.0,
        imported = 0.088,
        battery = 35
      ),
      Energy(
        exportProduced = 1.864,
        mainProduced = 1.124,
        consumed = 1.084,
        charged = 0.54,
        discharged = 0.404,
        mainExported = 0.0,
        imported = 0.096,
        battery = 35
      ),
      Energy(
        exportProduced = 2.12,
        mainProduced = 1.724,
        consumed = 0.636,
        charged = 1.188,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.1,
        battery = 36
      ),
      Energy(
        exportProduced = 2.324,
        mainProduced = 2.252,
        consumed = 1.076,
        charged = 1.352,
        discharged = 0.004,
        mainExported = 0.0,
        imported = 0.172,
        battery = 38
      ),
      Energy(
        exportProduced = 2.316,
        mainProduced = 2.78,
        consumed = 0.776,
        charged = 2.04,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.036,
        battery = 41
      ),
      Energy(
        exportProduced = 2.34,
        mainProduced = 3.004,
        consumed = 0.748,
        charged = 2.368,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.112,
        battery = 43
      ),
      Energy(
        exportProduced = 2.408,
        mainProduced = 3.564,
        consumed = 0.968,
        charged = 2.844,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.248,
        battery = 46
      ),
      Energy(
        exportProduced = 2.688,
        mainProduced = 3.892,
        consumed = 1.356,
        charged = 2.772,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.236,
        battery = 50
      ),
      Energy(
        exportProduced = 3.076,
        mainProduced = 4.42,
        consumed = 1.272,
        charged = 3.444,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.296,
        battery = 54
      ),
      Energy(
        exportProduced = 3.344,
        mainProduced = 4.584,
        consumed = 1.044,
        charged = 3.792,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.252,
        battery = 57
      ),
      Energy(
        exportProduced = 3.628,
        mainProduced = 5.076,
        consumed = 1.264,
        charged = 4.064,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.252,
        battery = 61
      ),
      Energy(
        exportProduced = 3.756,
        mainProduced = 5.6,
        consumed = 0.92,
        charged = 4.864,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.184,
        battery = 69
      ),
      Energy(
        exportProduced = 3.852,
        mainProduced = 5.92,
        consumed = 0.944,
        charged = 5.148,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.172,
        battery = 74
      ),
      Energy(
        exportProduced = 3.908,
        mainProduced = 6.14,
        consumed = 0.924,
        charged = 5.36,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.144,
        battery = 79
      ),
      Energy(
        exportProduced = 3.972,
        mainProduced = 6.48,
        consumed = 1.02,
        charged = 5.212,
        discharged = 0.0,
        mainExported = 0.248,
        imported = 0.0,
        battery = 87
      ),
      Energy(
        exportProduced = 4.04,
        mainProduced = 6.504,
        consumed = 0.936,
        charged = 2.948,
        discharged = 0.0,
        mainExported = 2.62,
        imported = 0.0,
        battery = 89
      ),
      Energy(
        exportProduced = 4.124,
        mainProduced = 2.976,
        consumed = 0.896,
        charged = 0.268,
        discharged = 0.0,
        mainExported = 1.812,
        imported = 0.0,
        battery = 89
      ),
      Energy(
        exportProduced = 4.1,
        mainProduced = 1.98,
        consumed = 0.952,
        charged = 0.104,
        discharged = 0.0,
        mainExported = 0.924,
        imported = 0.0,
        battery = 95
      ),
      Energy(
        exportProduced = 4.084,
        mainProduced = 1.416,
        consumed = 1.388,
        charged = 0.124,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.096,
        battery = 95
      ),
      Energy(
        exportProduced = 4.092,
        mainProduced = 1.048,
        consumed = 0.956,
        charged = 0.108,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 0.016,
        battery = 95
      ),
      Energy(
        exportProduced = 4.056,
        mainProduced = 1.292,
        consumed = 0.968,
        charged = 0.108,
        discharged = 0.004,
        mainExported = 0.22,
        imported = 0.0,
        battery = 95
      ),
      Energy(
        exportProduced = 4.02,
        mainProduced = 3.78,
        consumed = 3.616,
        charged = 0.08,
        discharged = 0.024,
        mainExported = 0.108,
        imported = 0.0,
        battery = 100
      ),
      Energy(
        exportProduced = 3.984,
        mainProduced = 6.744,
        consumed = 7.224,
        charged = 0.112,
        discharged = 0.392,
        mainExported = 0.0,
        imported = 0.2,
        battery = 99
      ),
      Energy(
        exportProduced = 3.932,
        mainProduced = 6.38,
        consumed = 6.7,
        charged = 0.224,
        discharged = 0.216,
        mainExported = 0.0,
        imported = 0.328,
        battery = 99
      ),
      Energy(
        exportProduced = 3.848,
        mainProduced = 2.344,
        consumed = 1.664,
        charged = 0.672,
        discharged = 0.088,
        mainExported = 0.1,
        imported = 0.0,
        battery = 100
      ),
      Energy(
        exportProduced = 3.76,
        mainProduced = 4.94,
        consumed = 5.32,
        charged = 0.112,
        discharged = 0.264,
        mainExported = 0.0,
        imported = 0.228,
        battery = 99
      ),
      Energy(
        exportProduced = 3.66,
        mainProduced = 6.092,
        consumed = 6.556,
        charged = 0.004,
        discharged = 0.356,
        mainExported = 0.0,
        imported = 0.112,
        battery = 99
      ),
      Energy(
        exportProduced = 3.568,
        mainProduced = 5.468,
        consumed = 6.536,
        charged = 0.0,
        discharged = 0.896,
        mainExported = 0.0,
        imported = 0.172,
        battery = 98
      ),
      Energy(
        exportProduced = 3.448,
        mainProduced = 5.344,
        consumed = 5.696,
        charged = 0.456,
        discharged = 0.776,
        mainExported = 0.0,
        imported = 0.032,
        battery = 98
      ),
      Energy(
        exportProduced = 3.308,
        mainProduced = 3.472,
        consumed = 1.484,
        charged = 2.232,
        discharged = 0.164,
        mainExported = 0.0,
        imported = 0.084,
        battery = 100
      ),
      Energy(
        exportProduced = 3.148,
        mainProduced = 4.708,
        consumed = 6.424,
        charged = 0.0,
        discharged = 1.516,
        mainExported = 0.0,
        imported = 0.196,
        battery = 98
      ),
      Energy(
        exportProduced = 2.976,
        mainProduced = 4.716,
        consumed = 7.852,
        charged = 0.0,
        discharged = 2.984,
        mainExported = 0.0,
        imported = 0.152,
        battery = 94
      ),
      Energy(
        exportProduced = 2.72,
        mainProduced = 3.912,
        consumed = 4.324,
        charged = 1.136,
        discharged = 1.472,
        mainExported = 0.0,
        imported = 0.076,
        battery = 93
      ),
      Energy(
        exportProduced = 2.164,
        mainProduced = 2.648,
        consumed = 1.384,
        charged = 1.412,
        discharged = 0.028,
        mainExported = 0.0,
        imported = 0.12,
        battery = 95
      ),
      Energy(
        exportProduced = 1.552,
        mainProduced = 1.508,
        consumed = 4.236,
        charged = 0.02,
        discharged = 2.652,
        mainExported = 0.0,
        imported = 0.096,
        battery = 91
      ),
      Energy(
        exportProduced = 1.272,
        mainProduced = 0.988,
        consumed = 7.428,
        charged = 0.0,
        discharged = 6.264,
        mainExported = 0.0,
        imported = 0.176,
        battery = 84
      ),
      Energy(
        exportProduced = 0.924,
        mainProduced = 0.46,
        consumed = 3.656,
        charged = 0.008,
        discharged = 3.016,
        mainExported = 0.0,
        imported = 0.188,
        battery = 79
      ),
      Energy(
        exportProduced = 0.716,
        mainProduced = 0.32,
        consumed = 8.208,
        charged = 0.0,
        discharged = 6.472,
        mainExported = 0.0,
        imported = 1.416,
        battery = 71
      ),
      Energy(
        exportProduced = 0.368,
        mainProduced = 0.252,
        consumed = 6.616,
        charged = 0.0,
        discharged = 6.284,
        mainExported = 0.0,
        imported = 0.08,
        battery = 64
      ),
      Energy(
        exportProduced = 0.2,
        mainProduced = 0.216,
        consumed = 5.728,
        charged = 0.004,
        discharged = 5.464,
        mainExported = 0.0,
        imported = 0.052,
        battery = 58
      ),
      Energy(
        exportProduced = 0.16,
        mainProduced = 0.188,
        consumed = 2.66,
        charged = 0.0,
        discharged = 2.288,
        mainExported = 0.0,
        imported = 0.184,
        battery = 53
      ),
      Energy(
        exportProduced = 0.06,
        mainProduced = 0.156,
        consumed = 6.588,
        charged = 0.0,
        discharged = 6.348,
        mainExported = 0.0,
        imported = 0.084,
        battery = 46
      ),
      Energy(
        exportProduced = 0.024,
        mainProduced = 0.124,
        consumed = 6.444,
        charged = 0.0,
        discharged = 6.24,
        mainExported = 0.0,
        imported = 0.08,
        battery = 39
      ),
      Energy(
        exportProduced = 0.02,
        mainProduced = 0.096,
        consumed = 6.132,
        charged = 0.0,
        discharged = 5.964,
        mainExported = 0.0,
        imported = 0.072,
        battery = 31
      ),
      Energy(
        exportProduced = 0.016,
        mainProduced = 0.076,
        consumed = 1.396,
        charged = 0.004,
        discharged = 1.212,
        mainExported = 0.0,
        imported = 0.116,
        battery = 29
      ),
      Energy(
        exportProduced = 0.012,
        mainProduced = 0.052,
        consumed = 1.108,
        charged = 0.0,
        discharged = 0.972,
        mainExported = 0.0,
        imported = 0.084,
        battery = 28
      ),
      Energy(
        exportProduced = 0.008,
        mainProduced = 0.032,
        consumed = 1.152,
        charged = 0.0,
        discharged = 1.036,
        mainExported = 0.0,
        imported = 0.084,
        battery = 27
      ),
      Energy(
        exportProduced = 0.008,
        mainProduced = 0.008,
        consumed = 1.26,
        charged = 0.0,
        discharged = 1.184,
        mainExported = 0.0,
        imported = 0.068,
        battery = 25
      ),
      Energy(
        exportProduced = 0.004,
        mainProduced = 0.004,
        consumed = 2.848,
        charged = 0.0,
        discharged = 2.78,
        mainExported = 0.0,
        imported = 0.064,
        battery = 22
      ),
      Energy(
        exportProduced = 0.004,
        mainProduced = 0.0,
        consumed = 1.248,
        charged = 0.004,
        discharged = 0.98,
        mainExported = 0.0,
        imported = 0.272,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.604,
        charged = 0.024,
        discharged = 0.008,
        mainExported = 0.0,
        imported = 1.62,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.632,
        charged = 0.04,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.672,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.18,
        charged = 0.036,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.216,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 2.272,
        charged = 0.028,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 2.3,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.204,
        charged = 0.044,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.248,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.32,
        charged = 0.024,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.344,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 2.604,
        charged = 0.052,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 2.656,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 2.84,
        charged = 0.344,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 3.184,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 2.836,
        charged = 0.064,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 2.9,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.588,
        charged = 0.024,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.612,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.896,
        charged = 0.04,
        discharged = 0.004,
        mainExported = 0.0,
        imported = 0.932,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 0.992,
        charged = 0.04,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.032,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 2.968,
        charged = 0.024,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 2.992,
        battery = 20
      ),
      Energy(
        exportProduced = 0.0,
        mainProduced = 0.0,
        consumed = 1.112,
        charged = 0.064,
        discharged = 0.0,
        mainExported = 0.0,
        imported = 1.176,
        battery = 20
      ),

      )
  )
}