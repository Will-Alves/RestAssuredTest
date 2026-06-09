package br.ce.wcaquino.rest.tests.refac.suite;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

import br.ce.wcaquino.rest.tests.refac.AuthTest;
import br.ce.wcaquino.rest.tests.refac.ContasTest;
import br.ce.wcaquino.rest.tests.refac.MovimentacaoTest;
import br.ce.wcaquino.rest.tests.refac.SaldoTest;

@Suite
@SelectClasses({
        ContasTest.class,
        MovimentacaoTest.class,
        SaldoTest.class,
        AuthTest.class
})
public class SuiteRefac {
}
