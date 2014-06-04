(function() {
    window.WallTime || (window.WallTime = {});
    window.WallTime.data = {
        rules: {"C-Eur":[{"name":"C-Eur","_from":"1916","_to":"only","type":"-","in":"Apr","on":"30","at":"23:00","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1916","_to":"only","type":"-","in":"Oct","on":"1","at":"1:00","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1917","_to":"1918","type":"-","in":"Apr","on":"Mon>=15","at":"2:00s","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1917","_to":"1918","type":"-","in":"Sep","on":"Mon>=15","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1940","_to":"only","type":"-","in":"Apr","on":"1","at":"2:00s","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1942","_to":"only","type":"-","in":"Nov","on":"2","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1943","_to":"only","type":"-","in":"Mar","on":"29","at":"2:00s","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1943","_to":"only","type":"-","in":"Oct","on":"4","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1944","_to":"1945","type":"-","in":"Apr","on":"Mon>=1","at":"2:00s","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1944","_to":"only","type":"-","in":"Oct","on":"2","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1945","_to":"only","type":"-","in":"Sep","on":"16","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1977","_to":"1980","type":"-","in":"Apr","on":"Sun>=1","at":"2:00s","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1977","_to":"only","type":"-","in":"Sep","on":"lastSun","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1978","_to":"only","type":"-","in":"Oct","on":"1","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1979","_to":"1995","type":"-","in":"Sep","on":"lastSun","at":"2:00s","_save":"0","letter":"-"},{"name":"C-Eur","_from":"1981","_to":"max","type":"-","in":"Mar","on":"lastSun","at":"2:00s","_save":"1:00","letter":"S"},{"name":"C-Eur","_from":"1996","_to":"max","type":"-","in":"Oct","on":"lastSun","at":"2:00s","_save":"0","letter":"-"}],"Austria":[{"name":"Austria","_from":"1920","_to":"only","type":"-","in":"Apr","on":"5","at":"2:00s","_save":"1:00","letter":"S"},{"name":"Austria","_from":"1920","_to":"only","type":"-","in":"Sep","on":"13","at":"2:00s","_save":"0","letter":"-"},{"name":"Austria","_from":"1946","_to":"only","type":"-","in":"Apr","on":"14","at":"2:00s","_save":"1:00","letter":"S"},{"name":"Austria","_from":"1946","_to":"1948","type":"-","in":"Oct","on":"Sun>=1","at":"2:00s","_save":"0","letter":"-"},{"name":"Austria","_from":"1947","_to":"only","type":"-","in":"Apr","on":"6","at":"2:00s","_save":"1:00","letter":"S"},{"name":"Austria","_from":"1948","_to":"only","type":"-","in":"Apr","on":"18","at":"2:00s","_save":"1:00","letter":"S"},{"name":"Austria","_from":"1980","_to":"only","type":"-","in":"Apr","on":"6","at":"0:00","_save":"1:00","letter":"S"},{"name":"Austria","_from":"1980","_to":"only","type":"-","in":"Sep","on":"28","at":"0:00","_save":"0","letter":"-"}],"EU":[{"name":"EU","_from":"1977","_to":"1980","type":"-","in":"Apr","on":"Sun>=1","at":"1:00u","_save":"1:00","letter":"S"},{"name":"EU","_from":"1977","_to":"only","type":"-","in":"Sep","on":"lastSun","at":"1:00u","_save":"0","letter":"-"},{"name":"EU","_from":"1978","_to":"only","type":"-","in":"Oct","on":"1","at":"1:00u","_save":"0","letter":"-"},{"name":"EU","_from":"1979","_to":"1995","type":"-","in":"Sep","on":"lastSun","at":"1:00u","_save":"0","letter":"-"},{"name":"EU","_from":"1981","_to":"max","type":"-","in":"Mar","on":"lastSun","at":"1:00u","_save":"1:00","letter":"S"},{"name":"EU","_from":"1996","_to":"max","type":"-","in":"Oct","on":"lastSun","at":"1:00u","_save":"0","letter":"-"}]},
        zones: {"Europe/Vienna":[{"name":"Europe/Vienna","_offset":"1:05:20","_rule":"-","format":"LMT","_until":"1893 Apr"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"C-Eur","format":"CE%sT","_until":"1920"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"Austria","format":"CE%sT","_until":"1940 Apr 1 2:00s"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"C-Eur","format":"CE%sT","_until":"1945 Apr 2 2:00s"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"1:00","format":"CEST","_until":"1945 Apr 12 2:00s"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"-","format":"CET","_until":"1946"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"Austria","format":"CE%sT","_until":"1981"},{"name":"Europe/Vienna","_offset":"1:00","_rule":"EU","format":"CE%sT","_until":""}]}
    };
    window.WallTime.autoinit = true;
}).call(this);