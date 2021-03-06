
package com.bank.retailbanking.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.bank.retailbanking.constants.ApplicationConstants;
import com.bank.retailbanking.dto.FundTransferRequestDto;
import com.bank.retailbanking.dto.FundTransferResponseDto;
import com.bank.retailbanking.dto.MortgageAccountSummaryResponse;
import com.bank.retailbanking.dto.MortgageAccountSummaryResponsedto;
import com.bank.retailbanking.entity.Customer;
import com.bank.retailbanking.entity.CustomerAccountDetail;
import com.bank.retailbanking.exception.AmountInvalidException;
import com.bank.retailbanking.exception.CustomerNotFoundException;
import com.bank.retailbanking.exception.GeneralException;
import com.bank.retailbanking.exception.MortgageException;
import com.bank.retailbanking.exception.SameAccountNumberException;
import com.bank.retailbanking.repository.CustomerAccountDetailRepository;
import com.bank.retailbanking.repository.CustomerRepository;
import com.bank.retailbanking.repository.CustomerTransactionsRepository;
import com.bank.retailbanking.service.TransactionServiceImpl;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TransactionServiceTest {

	@InjectMocks
	TransactionServiceImpl transactionServiceImpl;

	@Mock
	CustomerTransactionsRepository customerTransactionsRepository;

	@Mock
	CustomerAccountDetailRepository customerAccountDetailRepository;

	@Mock
	CustomerRepository customerRepository;

	Customer customer = null;
	MortgageAccountSummaryResponsedto mortgageAccountSummaryResponsedto = null;
	List<CustomerAccountDetail> customerAccountDetails = null;
	CustomerAccountDetail customerAccountDetail = null;
	List<MortgageAccountSummaryResponse> mortgageAccountSummaryResponses = null;
	MortgageAccountSummaryResponse mortgageAccountSummaryResponse = null;

	@Before
	public void before() {
		customer = new Customer();
		mortgageAccountSummaryResponsedto = new MortgageAccountSummaryResponsedto();
		customerAccountDetails = new ArrayList<>();
		customerAccountDetail = new CustomerAccountDetail();
		mortgageAccountSummaryResponses = new ArrayList<>();
		mortgageAccountSummaryResponse = new MortgageAccountSummaryResponse();
		customerAccountDetail.setAccountNumber(100002L);
		customerAccountDetail.setAccountOpeningDate(LocalDate.now());
		customerAccountDetail.setAccountType(ApplicationConstants.FUND_TRANSFER_ACCOUNT_TYPE);
		customerAccountDetail.setAvailableBalance(4000.00);
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetails.add(customerAccountDetail);
		mortgageAccountSummaryResponse.setAccountBalance(customerAccountDetail.getAvailableBalance());
		mortgageAccountSummaryResponse.setAccountNumber(customerAccountDetail.getAccountNumber());
		mortgageAccountSummaryResponse.setAccountType(customerAccountDetail.getAccountType());
		mortgageAccountSummaryResponses.add(mortgageAccountSummaryResponse);
		mortgageAccountSummaryResponsedto.setAccountDetails(mortgageAccountSummaryResponses);
		customer.setCustomerId(1001L);
	}

	@Test(expected = GeneralException.class)
	public void testGetAccountSummaryForNullCustomer() throws GeneralException {
		Optional<Customer> customer = Optional.ofNullable(null);
		Mockito.when(customerRepository.findById(1L)).thenReturn(customer);
		mortgageAccountSummaryResponsedto = transactionServiceImpl.getAccountSummary(1L);
		assertNull(mortgageAccountSummaryResponsedto);
	}

	@Test
	public void testGetAccountSummaryForPositive() throws GeneralException {
		Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findAllByCustomerId(customer)).thenReturn(customerAccountDetails);
		MortgageAccountSummaryResponsedto response = transactionServiceImpl.getAccountSummary(1L);
		assertNotNull(response);
	}

	@Test(expected = CustomerNotFoundException.class)
	public void testCustomerNotPresent()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		String message = ApplicationConstants.CUSTOMER_NOT_FOUND_MESSAGE;
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(2L);
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.ofNullable(null));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = CustomerNotFoundException.class)
	public void testCustomerIdNotPresent()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(2L);
		String message = ApplicationConstants.CUSTOMER_NOT_FOUND_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = CustomerNotFoundException.class)
	public void testMortgageAccountDetailPresent()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Mortgage");
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		String message = ApplicationConstants.CUSTOMER_NOT_FOUND_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				customerAccountDetail.getAccountType())).thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = CustomerNotFoundException.class)
	public void testSavingsNotPresent()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Food");
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		String message = ApplicationConstants.CUSTOMER_NOT_FOUND_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				customerAccountDetail.getAccountType())).thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = AmountInvalidException.class)
	public void testMortgageAndSavingsLessAvailableBalance()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(2000.00);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(0.0);
		String message = ApplicationConstants.AMOUNT_LESSBALANCE_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				customerAccountDetail.getAccountType())).thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = AmountInvalidException.class)
	public void testMortgageAndSavingsLessMinimumBalance()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(4000.00);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(2000.0);
		String message = ApplicationConstants.AMOUNT_LESSBALANCE_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				customerAccountDetail.getAccountType())).thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = AmountInvalidException.class)
	public void testSavingsBalanceException()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(5000.00);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(6000.0);
		String message = ApplicationConstants.AMOUNT_LESSBALANCE_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				customerAccountDetail.getAccountType())).thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test
	public void testMortgageAndSavingsSuccess()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(5000.00);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(1000.0);
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				customerAccountDetail.getAccountType())).thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(true, expected.isPresent());
	}

	@Test(expected = CustomerNotFoundException.class)
	public void testSavingsAccountInvalid()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		CustomerAccountDetail customerAccountDetail1 = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail1.setAccountNumber(100L);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setCreditAccount(101L);
		fundTransferRequestDto.setTransactionPurpose("savings");
		String message = ApplicationConstants.CUSTOMER_NOT_FOUND_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = CustomerNotFoundException.class)
	public void testSavingsCreditAccountInvalid()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountNumber(101L);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		String message = ApplicationConstants.CUSTOMER_NOT_FOUND_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.ofNullable(null));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer))
				.thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.ofNullable(null));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = SameAccountNumberException.class)
	public void testSavingsSameAccountInvalid()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountNumber(101L);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setCreditAccount(101L);
		String message = ApplicationConstants.ACCOUNTNUMBER_INVALID_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.ofNullable(null));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer))
				.thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.of(customerAccountDetail));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = AmountInvalidException.class)
	public void testSavingsLessAvailableBalance()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		CustomerAccountDetail customerAccountDetail1 = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(2000.00);
		customerAccountDetail1.setAccountNumber(101L);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setCreditAccount(102L);
		fundTransferRequestDto.setAmount(0.0);
		String message = ApplicationConstants.AMOUNT_LESSBALANCE_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.ofNullable(null));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer))
				.thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.of(customerAccountDetail1));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = AmountInvalidException.class)
	public void testSavingsLessMinimumBalance()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		CustomerAccountDetail customerAccountDetail1 = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(4000.00);
		customerAccountDetail1.setAccountNumber(101L);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(2000.0);
		fundTransferRequestDto.setCreditAccount(102L);
		String message = ApplicationConstants.AMOUNT_LESSBALANCE_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.ofNullable(null));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer))
				.thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.of(customerAccountDetail1));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test(expected = AmountInvalidException.class)
	public void testSavingsLessBalanceException()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		CustomerAccountDetail customerAccountDetail1 = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(5000.00);
		customerAccountDetail1.setAccountNumber(101L);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(6000.0);
		fundTransferRequestDto.setCreditAccount(102L);
		String message = ApplicationConstants.AMOUNT_LESSBALANCE_MESSAGE;
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.ofNullable(null));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer))
				.thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.of(customerAccountDetail1));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(message, expected);
	}

	@Test
	public void testSavingsSuccess()
			throws CustomerNotFoundException, AmountInvalidException, SameAccountNumberException, MortgageException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		CustomerAccountDetail customerAccountDetail1 = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		customerAccountDetail.setAccountType("Savings");
		customerAccountDetail.setAvailableBalance(100000.00);
		customerAccountDetail1.setAccountNumber(101L);
		customerAccountDetail1.setAvailableBalance(200.00);
		FundTransferRequestDto fundTransferRequestDto = new FundTransferRequestDto();
		fundTransferRequestDto.setCustomerId(1L);
		fundTransferRequestDto.setTransactionPurpose("Mortgage");
		fundTransferRequestDto.setAmount(6000.0);
		fundTransferRequestDto.setCreditAccount(102L);
		Mockito.when(customerRepository.findByCustomerId(fundTransferRequestDto.getCustomerId()))
				.thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerIdAndAccountType(customer,
				fundTransferRequestDto.getTransactionPurpose())).thenReturn(Optional.ofNullable(null));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer))
				.thenReturn(Optional.of(customerAccountDetail));
		Mockito.when(customerAccountDetailRepository.findByAccountNumber(fundTransferRequestDto.getCreditAccount()))
				.thenReturn(Optional.of(customerAccountDetail1));
		Optional<FundTransferResponseDto> expected = transactionServiceImpl.fundTransfer(fundTransferRequestDto);
		assertEquals(true, expected.isPresent());
	}

	@Test(expected = GeneralException.class)
	public void testGetAccountSummarysNegative() throws GeneralException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		Mockito.when(customerRepository.findById(6L)).thenReturn(Optional.of(customer));
		transactionServiceImpl.getAccountSummary(1L);
	}

	@Test(expected = GeneralException.class)
	public void testGetAccountSummarysCustDetailsNegative() throws GeneralException {
		Customer customer = new Customer();
		customer.setCustomerId(1L);
		Customer customer1 = new Customer();
		customer1.setCustomerId(2L);
		CustomerAccountDetail customerAccountDetail = new CustomerAccountDetail();
		customerAccountDetail.setCustomerId(customer);
		Mockito.when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
		Mockito.when(customerAccountDetailRepository.findByCustomerId(customer1))
				.thenReturn(Optional.of(customerAccountDetail));
		transactionServiceImpl.getAccountSummarys(1L);
	}
}