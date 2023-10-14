package net.finiasz.pendu.database

//import android.util.Log
//import net.finiasz.pendu.database.AppDatabase
//import net.finiasz.pendu.database.Word
//import net.finiasz.pendu.database.getLongueur
//
//
//class DbLoader {
//    companion object {
//        fun populate() {
//            Thread {
//                val db = AppDatabase.getInstance()
//                App.instance.applicationContext.assets.list("dicts")?.forEach { dict_txt ->
//                    Log.d("populate", "Parsing dictionary: $dict_txt")
//
//                    App.instance.applicationContext.assets.open("dicts/$dict_txt").use { ist ->
//                        ist.bufferedReader().use {br ->
//                            var count = 0
//                            br.forEachLine {
//                                val mot = it.trim()
//                                val word = db.wordDao().get(mot)
//                                if (word == null) {
//                                    db.wordDao().insert(Word(
//                                        mot = mot,
//                                        longueur = mot.getLongueur(),
//                                        fr_xs = "fr_xs.txt".contentEquals(dict_txt),
//                                        fr_m = "fr_m.txt".contentEquals(dict_txt),
//                                        fr_l = "fr_l.txt".contentEquals(dict_txt),
//                                        fr_xxl = "fr_xxl.txt".contentEquals(dict_txt),
//                                        en_xs = "en_xs.txt".contentEquals(dict_txt),
//                                        en_s = "en_s.txt".contentEquals(dict_txt),
//                                        en_l = "en_l.txt".contentEquals(dict_txt),
//                                    ))
//                                } else {
//                                    when(dict_txt) {
//                                        "fr_xs.txt" -> db.wordDao().markFrXs(mot)
//                                        "fr_m.txt" -> db.wordDao().markFrM(mot)
//                                        "fr_l.txt" -> db.wordDao().markFrL(mot)
//                                        "fr_xxl.txt" -> db.wordDao().markFrXxl(mot)
//                                        "en_xs.txt" -> db.wordDao().markEnXs(mot)
//                                        "en_s.txt" -> db.wordDao().markEnS(mot)
//                                        "en_l.txt" -> db.wordDao().markEnL(mot)
//                                        else -> Log.e("populate", "What? $dict_txt : $mot")
//                                    }
//                                }
//                                count++
//                                if (count % 1000 == 0) {
//                                    Log.d("populate","Progress $count")
//                                }
//                            }
//                        }
//                    }
//                }
//            }.start()
//        }
//    }
//}