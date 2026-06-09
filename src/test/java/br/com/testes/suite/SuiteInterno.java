package br.com.testes.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import br.com.testes.SicsDynaMetrica;
import br.com.testes.SCC_Teste;

@Suite
@SelectClasses({
        SCC_Teste.class,
        SicsDynaMetrica.class
})
public class SuiteInterno {
}
